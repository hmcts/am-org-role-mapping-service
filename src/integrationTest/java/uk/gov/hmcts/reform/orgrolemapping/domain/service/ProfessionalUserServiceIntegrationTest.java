package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class ProfessionalUserServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService professionalUserService;

    @SpyBean
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @SpyBean
    private AccessTypesRepository accessTypesRepository;

    @MockBean
    private ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @MockBean
    private PRDService prdService;

    @MockBean
    private ProcessEventTracker processEventTracker;

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String USER_ID = "1";

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private ProcessMonitorDto processMonitorDto;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
        wiremockFixtures.stubIdamCall();
        processMonitorDto = new ProcessMonitorDto("PRM Process 6 - Refresh users - Batch mode [Test]");
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
    void shouldRefreshUsers() {
        professionalUserService.refreshUsers(processMonitorDto);

        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity refreshedUser = userRefreshQueueEntities.stream()
                .filter(entity -> entity.getUserId().equals(USER_ID))
                .findFirst()
                .orElse(null);


        assertNotNull(refreshedUser);
        assertEquals(USER_ID, refreshedUser.getUserId());
        assertNotNull(refreshedUser.getUserLastUpdated().toString());
        assertEquals(1, refreshedUser.getAccessTypesMinVersion());
        assertNull(refreshedUser.getDeleted());
        assertEquals("[{\"enabled\": true, \"accessTypeId\": \"1\", \"jurisdictionId\": "
                        + "\"BEFTA_JURISDICTION_1\", \"organisationProfileId\": \"SOLICITOR_PROFILE\"}, "
                        + "{\"enabled\": true, \"accessTypeId\": \"2\", \"jurisdictionId\": \"BEFTA_JURISDICTION_2\", "
                        + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]",
                refreshedUser.getAccessTypes());
        assertEquals("OrgId", refreshedUser.getOrganisationId());
        assertEquals("ACTIVE", refreshedUser.getOrganisationStatus());
        assertArrayEquals(new String[]{"SOLICITOR_PROFILE", "2"},
                refreshedUser.getOrganisationProfileIds());
        assertFalse(refreshedUser.getActive());

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
    void shouldRollback_AndUpdateRetryToOneOnException() {
        doThrow(ServiceException.class).when(professionalRefreshOrchestrationHelper).refreshSingleUser(any(), any());

        professionalUserService.refreshUsers(processMonitorDto);

        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        assertTrue(userRefreshQueueEntities.get(0).getActive());
        assertEquals(1, userRefreshQueueEntities.get(0).getRetry());
        assertTrue(userRefreshQueueEntities.get(0).getRetryAfter().isAfter(LocalDateTime.now()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138_retry_3.sql"})
    void shouldRollback_AndUpdateRetryToFourAndRetryAfterToNullOnException() {
        doThrow(ServiceException.class).when(userRefreshQueueRepository).clearUserRefreshRecord(any(), any(), any());

        professionalUserService.refreshUsers(processMonitorDto);

        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        assertTrue(userRefreshQueueEntities.get(0).getActive());
        assertEquals(4, userRefreshQueueEntities.get(0).getRetry());
        assertNull(userRefreshQueueEntities.get(0).getRetryAfter());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138_retry_3.sql"})
    void shouldClearRefreshFailedRecord_whenRefreshIsSuccessfulOnRetryAttempt() {
        professionalUserService.refreshUsers(processMonitorDto);

        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        assertFalse(userRefreshQueueEntities.get(0).getActive());
        assertEquals(0, userRefreshQueueEntities.get(0).getRetry());
        assertNull(userRefreshQueueEntities.get(0).getRetryAfter());
    }

    @Test
    void shouldReturnTrue_whenNoEntitiesToProcess() {
        // arrange
        userRefreshQueueRepository.deleteAll();

        // act
        boolean result = professionalUserService.refreshUsers(processMonitorDto);

        // assert
        assertTrue(result);
        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        assertTrue(userRefreshQueueEntities.isEmpty());
        processMonitorDtoArgumentCaptor.getAllValues().forEach(dto -> {
            assertEquals("No entities to process", dto.getProcessSteps().toString());
        });
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
    void shouldThrowException_whenNoAccessTypeEntityFound() {
        // arrange
        accessTypesRepository.deleteAll();

        // act
        Exception exception = assertThrows(ServiceException.class, () ->
                professionalUserService.refreshUsers(processMonitorDto));

        // assert
        assertEquals("Single AccessTypesEntity not found", exception.getMessage());
    }
}
