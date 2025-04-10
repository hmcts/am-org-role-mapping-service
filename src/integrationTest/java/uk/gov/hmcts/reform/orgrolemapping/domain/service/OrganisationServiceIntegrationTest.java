package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Autowired
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

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

        assertEquals(1, organisationRefreshQueueRepository.findAll().size());
        assertEquals(0, profileRefreshQueueRepository.getActiveProfileEntities().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_profile_refresh_queue.sql"})
    void shouldInsertTwoOrganisationIntoOrganisationRefreshQueue_AndClearProfileRefreshQueue_WithPagination() {
        OrganisationInfo organisationInfo = buildOrganisationInfo(1);
        OrganisationByProfileIdsResponse page0
                = buildOrganisationByProfileIdsResponse(organisationInfo, "123", true);

        when(prdService.fetchOrganisationsByProfileIds(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page0));

        OrganisationInfo organisationInfo2 = buildOrganisationInfo(2);
        OrganisationByProfileIdsResponse page1
                = buildOrganisationByProfileIdsResponse(organisationInfo2, "456", false);

        when(prdService.fetchOrganisationsByProfileIds(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page1));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(2, organisationRefreshQueueRepository.findAll().size());
        assertEquals(0, profileRefreshQueueRepository.getActiveProfileEntities().size());
    }

    @Test
    void shouldInsertNoOrganisationIntoOrganisationRefreshQueue_AsThereIsNoActiveProfiles() {
        assertEquals(0, profileRefreshQueueRepository.getActiveProfileEntities().size());

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(0, organisationRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldFindOrganisationChangesAndInsertIntoOrganisationRefreshQueue() {
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationsResponse response = OrganisationsResponse.builder()
                .organisations(List.of(organisationInfo))
                .moreAvailable(false).build();

        when(prdService.retrieveOrganisations(any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

        assertEquals(1, organisationRefreshQueueRepository.findAll().size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    void shouldFindOrganisationChangesAndInsertIntoOrganisationRefreshQueue_WithPagination() {
        final LocalDateTime preTestLastBatchRunTime = getOrgLastBatchRunTime();

        // Arrange
        OrganisationInfo organisationInfo1 = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationsResponse page1 = OrganisationsResponse.builder()
                .organisations(List.of(organisationInfo1))
                .moreAvailable(true).build();

        when(prdService.retrieveOrganisations(any(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(page1));

        OrganisationInfo organisationInfo2 = OrganisationInfo.builder()
                .organisationIdentifier("456")
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationsResponse page2 = OrganisationsResponse.builder()
                .organisations(List.of(organisationInfo2))
                .moreAvailable(false).build();

        when(prdService.retrieveOrganisations(any(), eq(2), anyInt()))
                .thenReturn(ResponseEntity.ok(page2));

        // Act
        organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

        // Assert
        List<OrganisationRefreshQueueEntity> organisationEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(2, organisationEntities.size());

        LocalDateTime postTestLastBatchRunTime = getOrgLastBatchRunTime();

        assertTrue(postTestLastBatchRunTime.isAfter(preTestLastBatchRunTime));
    }

    private LocalDateTime getOrgLastBatchRunTime() {
        List<BatchLastRunTimestampEntity> allBatchLastRunTimestampEntities = batchLastRunTimestampRepository
                .findAll();
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = allBatchLastRunTimestampEntities.get(0);
        return batchLastRunTimestampEntity.getLastOrganisationRunDatetime();
    }
}
