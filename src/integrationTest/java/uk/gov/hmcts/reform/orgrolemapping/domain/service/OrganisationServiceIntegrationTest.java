package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildOrganisationByProfileIdsResponse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildOrganisationInfo;

@Transactional
public class OrganisationServiceIntegrationTest extends BaseTestIntegration {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @MockBean
    private PrdService prdService;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_profile_refresh_queue.sql"})
    void shouldInsertOneOrganisationIntoOrganisationRefreshQueue_AndClearProfileRefreshQueue() {
        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse response
                = buildOrganisationByProfileIdsResponse(organisationInfo, "123", false);

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 1);
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_profile_refresh_queue.sql"})
    void shouldInsertTwoOrganisationIntoOrganisationRefreshQueue_AndClearProfileRefreshQueue_WithPagination() {
        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse page1
                = buildOrganisationByProfileIdsResponse(organisationInfo, "123", true);

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        OrganisationByProfileIdsResponse page2
                = buildOrganisationByProfileIdsResponse(organisationInfo2, "456", false);

        when(prdService.fetchOrganisationsByProfileIds(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 2);
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);
    }

    @Test
    void shouldInsertNoOrganisationIntoOrganisationRefreshQueue_AsThereIsNoActiveProfiles() {
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql",
        "classpath:sql/insert_batch_last_run.sql"})
    void shouldFindOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationsResponse response = OrganisationsResponse.builder()
                .organisations(List.of(organisationInfo))
                .moreAvailable(false).build();

        when(prdService.retrieveOrganisations(any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 1);
    }

}
