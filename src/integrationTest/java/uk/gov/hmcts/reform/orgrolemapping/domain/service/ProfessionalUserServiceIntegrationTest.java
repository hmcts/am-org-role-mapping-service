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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUser;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUsersByOrganisationResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUsersOrganisationInfo;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.refreshUser;

public class ProfessionalUserServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService professionalUserService;

    @SpyBean
    private UserRefreshQueueRepository mockUserRefreshQueueRepository;

    @SpyBean
    private AccessTypesRepository accessTypesRepository;

    @MockBean
    private ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

    @MockBean
    private PrdService prdService;

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
            scripts = {"classpath:sql/insert_organisation_profiles.sql"})
    void shouldInsertOneUserIntoUserRefreshQueue_AndClearOrganisationRefreshQueue() {
        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(123, professionalUser);
        UsersByOrganisationResponse response =
                buildUsersByOrganisationResponse(usersOrganisationInfo, "1", "1", false);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

        assertEquals(1, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
                = organisationRefreshQueueRepository.findAll();
        assertFalse(organisationRefreshQueueEntities.get(0).getActive());

        List<UserRefreshQueueEntity> userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userRefreshEntity = userRefreshQueueEntities.get(0);

        assertEquals("1", userRefreshEntity.getUserId());
        assertNotNull(userRefreshEntity.getLastUpdated());
        assertNotNull(userRefreshEntity.getUserLastUpdated());
        assertNotNull(userRefreshEntity.getDeleted());
        assertEquals("[]", userRefreshEntity.getAccessTypes());
        assertEquals("123", userRefreshEntity.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity.getRetry());
        assertNotNull(userRefreshEntity.getRetryAfter());
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_multiple_organisations_profiles.sql"})
    void shouldInsertMultipleUserIntoUserRefreshQueue_AndClearOrganisationRefreshQueue_MultipleOrgEntity() {
        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(123, professionalUser);
        UsersByOrganisationRequest usersByOrganisationRequestOrg1 = new UsersByOrganisationRequest(List.of("123"));
        UsersByOrganisationResponse responseOrg1 =
            buildUsersByOrganisationResponse(usersOrganisationInfo, "1", "1", false);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null),eq(usersByOrganisationRequestOrg1)))
            .thenReturn(ResponseEntity.ok(responseOrg1));

        ProfessionalUser professionalUser2 = buildProfessionalUser(2);
        UsersOrganisationInfo usersOrganisationInfo2 = buildUsersOrganisationInfo(1234, professionalUser2);
        UsersByOrganisationRequest usersByOrganisationRequestOrg2 = new UsersByOrganisationRequest(List.of("1234"));
        UsersByOrganisationResponse responseOrg2 =
            buildUsersByOrganisationResponse(usersOrganisationInfo2, "1", "1", false);
        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null),eq(usersByOrganisationRequestOrg2)))
            .thenReturn(ResponseEntity.ok(responseOrg2));

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

        assertEquals(2, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
            = organisationRefreshQueueRepository.findAll();
        assertFalse(organisationRefreshQueueEntities.get(0).getActive());
        assertFalse(organisationRefreshQueueEntities.get(1).getActive());
        List<UserRefreshQueueEntity> userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userRefreshEntity1 = userRefreshQueueEntities.get(0);
        UserRefreshQueueEntity userRefreshEntity2 = userRefreshQueueEntities.get(1);

        assertEquals("1", userRefreshEntity1.getUserId());
        assertNotNull(userRefreshEntity1.getLastUpdated());
        assertNotNull(userRefreshEntity1.getUserLastUpdated());
        assertNotNull(userRefreshEntity1.getDeleted());
        assertEquals("[]", userRefreshEntity1.getAccessTypes());
        assertEquals("123", userRefreshEntity1.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity1.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity1.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity1.getRetry());
        assertNotNull(userRefreshEntity1.getRetryAfter());

        assertEquals("2", userRefreshEntity2.getUserId());
        assertNotNull(userRefreshEntity2.getLastUpdated());
        assertNotNull(userRefreshEntity2.getUserLastUpdated());
        assertNotNull(userRefreshEntity2.getDeleted());
        assertEquals("[]", userRefreshEntity2.getAccessTypes());
        assertEquals("1234", userRefreshEntity2.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity2.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity2.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity2.getRetry());
        assertNotNull(userRefreshEntity2.getRetryAfter());
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_organisation_profiles.sql"})
    void shouldInsertOneUserIntoUserRefreshQueue_ById() {
        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(123, professionalUser);
        UsersByOrganisationResponse response =
            buildUsersByOrganisationResponse(usersOrganisationInfo, "1", "1", false);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
            .thenReturn(ResponseEntity.ok(response));

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById("123");

        assertEquals(1, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
            = organisationRefreshQueueRepository.findAll();
        assertFalse(organisationRefreshQueueEntities.get(0).getActive());

        List<UserRefreshQueueEntity> userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userRefreshEntity = userRefreshQueueEntities.get(0);

        assertEquals("1", userRefreshEntity.getUserId());
        assertNotNull(userRefreshEntity.getLastUpdated());
        assertNotNull(userRefreshEntity.getUserLastUpdated());
        assertNotNull(userRefreshEntity.getDeleted());
        assertEquals("[]", userRefreshEntity.getAccessTypes());
        assertEquals("123", userRefreshEntity.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity.getRetry());
        assertNotNull(userRefreshEntity.getRetryAfter());
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
            scripts = {"classpath:sql/insert_organisation_profiles.sql"})
    void shouldRollback_AndUpdateRetryToOneOnException2() {
        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(123, professionalUser);
        UsersByOrganisationResponse page1 =
                buildUsersByOrganisationResponse(usersOrganisationInfo, "1", "1", true);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        // throwing exception on 2nd page retrieval to test rollback (user inserts from page 1 SHOULD be rolled back)
        when(prdService.fetchUsersByOrganisation(any(), any(String.class), any(String.class), any()))
                .thenThrow(ServiceException.class);

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

        assertEquals(0, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
                = organisationRefreshQueueRepository.findAll();
        assertTrue(organisationRefreshQueueEntities.get(0).getActive());
        assertEquals(1, organisationRefreshQueueEntities.get(0).getRetry());
        assertTrue(organisationRefreshQueueEntities.get(0).getRetryAfter().isAfter(LocalDateTime.now()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138_retry_3.sql"})
    void shouldRollback_AndUpdateRetryToFourAndRetryAfterToNullOnException() {
        doThrow(ServiceException.class).when(mockUserRefreshQueueRepository)
                .clearUserRefreshRecord(any(), any(), any());

        professionalUserService.refreshUsers(processMonitorDto);

        List<UserRefreshQueueEntity> userRefreshQueueEntities
                = userRefreshQueueRepository.findAll();
        assertTrue(userRefreshQueueEntities.get(0).getActive());
        assertEquals(4, userRefreshQueueEntities.get(0).getRetry());
        assertNull(userRefreshQueueEntities.get(0).getRetryAfter());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_organisation_profiles_retry_3.sql"})
    void shouldRollback_AndUpdateRetryToFourAndRetryAfterToNullOnException2() {
        userRefreshQueueRepository.deleteAll();
        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(123, professionalUser);
        UsersByOrganisationResponse page1 =
                buildUsersByOrganisationResponse(usersOrganisationInfo, "1", "1", true);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        // throwing exception on 2nd page retrieval to test rollback (user inserts from page 1 SHOULD be rolled back)
        when(prdService.fetchUsersByOrganisation(any(), any(String.class), any(String.class), any()))
                .thenThrow(ServiceException.class);

        ServiceException exception = assertThrows(ServiceException.class, () ->
                professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue()
        );

        assertEquals("Retry limit reached", exception.getMessage());

        assertEquals(0, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
                = organisationRefreshQueueRepository.findAll();
        assertTrue(organisationRefreshQueueEntities.get(0).getActive());
        assertEquals(4, organisationRefreshQueueEntities.get(0).getRetry());
        assertNull(organisationRefreshQueueEntities.get(0).getRetryAfter());
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
            scripts = {"classpath:sql/insert_access_types.sql",
                "classpath:sql/insert_batch_last_run.sql",
                "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithoutPagination() {
        userRefreshQueueRepository.deleteAll();
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(1, userRefreshQueueRepository.findAll().size());
        assertEquals(1, batchLastRunTimestampRepository.findAll().size());
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
            .isEqualTo(EndStatus.SUCCESS);
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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_access_types.sql",
                "classpath:sql/insert_batch_last_run.sql",
                "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithPagination() {
        userRefreshQueueRepository.deleteAll();
        final LocalDateTime preTestLastBatchRunTime = getLastUserRunDatetime();

        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", true);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        RefreshUser refreshUser2 = refreshUser(2);
        GetRefreshUserResponse response2 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser2, "456", false);

        when(prdService.retrieveUsers(any(), anyInt(), any(String.class)))
                .thenReturn(ResponseEntity.ok(response2));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(2, userRefreshQueueRepository.findAll().size());

        LocalDateTime postTestLastBatchRunTime = getLastUserRunDatetime();

        // assert the last batch run time has been updated
        assertTrue(postTestLastBatchRunTime.isAfter(preTestLastBatchRunTime));

        // assert prd service is invoked with the original last user run time (value from sql script, not to be
        // confused with last org run time)
        verify(prdService).retrieveUsers(eq("2024-02-01T12:31:56"), anyInt(), eq(null));
        verify(prdService).retrieveUsers(eq("2024-02-01T12:31:56"), anyInt(), eq("123"));
    }

    private LocalDateTime getLastUserRunDatetime() {
        List<BatchLastRunTimestampEntity> allBatchLastRunTimestampEntities = batchLastRunTimestampRepository
                .findAll();
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = allBatchLastRunTimestampEntities.get(0);
        return batchLastRunTimestampEntity.getLastUserRunDatetime();
    }
}
