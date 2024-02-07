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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationStaleProfilesResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationStaleProfilesResponse response = OrganisationStaleProfilesResponse.builder()
                .organisationInfo(List.of(organisationInfo))
                .lastRecordInPage("123")
                .moreAvailable(false).build();

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 1);
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_profile_refresh_queue.sql"})
    void shouldInsertTwoOrganisationIntoOrganisationRefreshQueue_AndClearProfileRefreshQueue() {
        OrganisationInfo organisationOne = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationInfo organisationTwo = OrganisationInfo.builder()
                .organisationIdentifier("456")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationStaleProfilesResponse response = OrganisationStaleProfilesResponse.builder()
                .organisationInfo(List.of(organisationOne, organisationTwo))
                .lastRecordInPage("456")
                .moreAvailable(false).build();

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 2);
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/insert_profile_refresh_queue.sql"})
    void shouldInsertTwoOrganisationIntoOrganisationRefreshQueue_AndClearProfileRefreshQueue_WithPagination() {
        OrganisationInfo organisationInfo = OrganisationInfo.builder()
                .organisationIdentifier("123")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationStaleProfilesResponse page1 = OrganisationStaleProfilesResponse.builder()
                .organisationInfo(List.of(organisationInfo))
                .lastRecordInPage("123")
                .moreAvailable(true).build();

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        OrganisationInfo organisationInfo2 = OrganisationInfo.builder()
                .organisationIdentifier("456")
                .status("ACTIVE")
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE")).build();

        OrganisationStaleProfilesResponse page2 = OrganisationStaleProfilesResponse.builder()
                .organisationInfo(List.of(organisationInfo2))
                .lastRecordInPage("456")
                .moreAvailable(false).build();

        when(prdService.fetchOrganisationsWithStaleProfiles(any(), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue();

        assertEquals(organisationRefreshQueueRepository.findAll().size(), 2);
        assertEquals(profileRefreshQueueRepository.getActiveProfileEntities().size(), 0);
    }
}
