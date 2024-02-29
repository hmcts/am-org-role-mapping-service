package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUser;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUsersByOrganisationResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildUsersOrganisationInfo;

public class ProfessionalUserIntegrationTest extends BaseTestIntegration {

    @Autowired
    private ProfessionalUserService professionalUserService;

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

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

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

        assertEquals(0, userRefreshQueueRepository.findAll().size());

        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities
                = organisationRefreshQueueRepository.findAll();
        assertTrue(organisationRefreshQueueEntities.get(0).getActive());
        assertEquals(4, organisationRefreshQueueEntities.get(0).getRetry());
        assertNull(organisationRefreshQueueEntities.get(0).getRetryAfter());
    }
}