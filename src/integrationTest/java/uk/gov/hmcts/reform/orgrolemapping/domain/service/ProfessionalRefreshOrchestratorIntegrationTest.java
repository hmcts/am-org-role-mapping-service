package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;

@Transactional
class ProfessionalRefreshOrchestratorIntegrationTest extends BaseTestIntegration {

    private static final String USER_ID = "1234";

    @Autowired
    private ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;

    @SpyBean
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @SpyBean
    private AccessTypesRepository accessTypesRepository;

    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();
    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
        wiremockFixtures.stubIdamCall();
    }



    @Nested
    class RefreshProfessionalUser {

        @BeforeEach
        void setUp() throws IOException {
            doReturn(ResponseEntity.ok(TestDataBuilder.buildRefreshUsersResponse(USER_ID)))
                .when(prdFeignClient).getRefreshUsers(any());

            doReturn(ResponseEntity.status(HttpStatus.CREATED).body("RoleAssignment"))
                    .when(rasFeignClient).createRoleAssignment(any(), any());
        }

        @Test
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
        void shouldRefreshProfessionalUser() {
            professionalRefreshOrchestrator.refreshProfessionalUser(USER_ID);

            UserRefreshQueueEntity refreshedUser = userRefreshQueueRepository.findByUserId(USER_ID);
            assertEquals(USER_ID, refreshedUser.getUserId());
            assertEquals("2023-11-20T15:51:33.046", refreshedUser.getLastUpdated().toString());
            assertEquals(2, refreshedUser.getAccessTypesMinVersion());
            assertNull(refreshedUser.getDeleted());
            assertEquals("[{\"enabled\": true, \"accessTypeId\": \"1234\", \"jurisdictionId\": \"12345\", " 
                    + "\"organisationProfileId\": \"SOLICITOR_PROFILE\"}]",
                refreshedUser.getAccessTypes());
            assertEquals("ORG1", refreshedUser.getOrganisationId());
            assertEquals("ACTIVE", refreshedUser.getOrganisationStatus());
            assertArrayEquals(new String[]{"SOLICITOR_PROFILE", "OTHER_PROFILE"},
                refreshedUser.getOrganisationProfileIds());
            assertFalse(refreshedUser.getActive());
        }

        @Test
        void shouldThrowExceptionWhenNoAccessTypesAvailable() {
            Exception exception = assertThrows(ServiceException.class, () ->
                professionalRefreshOrchestrator.refreshProfessionalUser(USER_ID));

            assertEquals(NO_ACCESS_TYPES_FOUND, exception.getMessage());
        }
    }

    @Nested
    class RefreshProfessionalUsers {

        @Test
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
        void refreshProfessionalUserBatch() {
            assertTrue(userRefreshQueueRepository.findByUserId("1").getActive());

            professionalRefreshOrchestrator.refreshProfessionalUsers();

            verify(accessTypesRepository, times(1)).findFirstByOrderByVersionDesc();
            verify(userRefreshQueueRepository, times(1)).findFirstByActiveTrue();
            assertFalse(userRefreshQueueRepository.findByUserId("1").getActive());
        }

        @Test
        void shouldThrowExceptionWhenNoAccessTypesAvailable() {
            Exception exception = assertThrows(ServiceException.class, () ->
                professionalRefreshOrchestrator.refreshProfessionalUsers());

            assertEquals(NO_ACCESS_TYPES_FOUND, exception.getMessage());
        }

        @Test
        @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
                scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
        void refreshProfessionalUser_GA138() {
            assertTrue(userRefreshQueueRepository.findByUserId("1").getActive());

            professionalRefreshOrchestrator.refreshProfessionalUsers();

            verify(accessTypesRepository, times(1)).findFirstByOrderByVersionDesc();
            verify(userRefreshQueueRepository, times(1)).findFirstByActiveTrue();
            assertFalse(userRefreshQueueRepository.findByUserId("1").getActive());
        }

    }
}