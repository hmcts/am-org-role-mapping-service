package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

    @MockBean
    private PrdService prdService;

    @BeforeEach
    void setUp() {
        userRefreshQueueRepository.deleteAll();
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
            scripts = {"classpath:sql/insert_organisation_profiles.sql"})
    void shouldRollback_AndUpdateRetryToOneOnException() {
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
            scripts = {"classpath:sql/insert_organisation_profiles_retry_3.sql"})
    void shouldRollback_AndUpdateRetryToFourAndRetryAfterToNullOnException() {
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
            scripts = {"classpath:sql/insert_access_types.sql",
                "classpath:sql/insert_batch_last_run.sql",
                "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithoutPagination() {
        RefreshUser refreshUser = refreshUser(1);
        GetRefreshUserResponse response1 = IntTestDataBuilder.buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), anyInt(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertEquals(1, userRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_access_types.sql",
                "classpath:sql/insert_batch_last_run.sql",
                "classpath:sql/insert_user_refresh_queue.sql"})
    void shouldFindUserChangesAndInsertIntoRefreshQueue_WithPagination() {
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