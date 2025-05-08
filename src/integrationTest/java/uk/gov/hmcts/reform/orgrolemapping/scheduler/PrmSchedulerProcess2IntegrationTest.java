package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@Slf4j
class PrmSchedulerProcess2IntegrationTest extends BaseSchedulerTestIntegration {

    private static final String CIVIL_SOLICITOR_0 = "CIVIL_SOLICITOR_0";
    private static final String CIVIL_SOLICITOR_1 = "CIVIL_SOLICITOR_1";
    private static final String PUBLICLAW_SOLICITOR_1 = "PUBLICLAW_SOLICITOR_1";
    private static final String PUBLICLAW_SOLICITOR_2 = "PUBLICLAW_SOLICITOR_2";

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty organisation list with no new organisation.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNoChange_emptyPrdResponse() {

        // verify that no organisations are updated
        runTest(List.of());

        // verify that the ProfileRefreshQueue remains empty
        assertTotalProfileRefreshQueueEntitiesInDb(0);

        // verify that the OrganisationRefreshQueue remains empty
        assertTotalOrganisationRefreshQueueEntitiesInDb(0);
    }

    /**
     * New Organisations - Insert three organisations to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/profile_refresh_queue/insert_Solicitor_Profile.sql",
        "classpath:sql/prm/organisation_refresh_queue/init_organisation_refresh_queue.sql"
    })
    void testNewOrganisation_singlePrdResponse() {

        // verify that the Organisations are updated (i.e. version 1) and has 1 organisation profile
        runTest(List.of(
            "/SchedulerTests/PrdOrganisationInfo/organisation1_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation2_scenario_01.json",
            "/SchedulerTests/PrdOrganisationInfo/organisation3_scenario_01.json"
        ));

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId and set to inactive
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, 1, false);

        // verify that the OranisationRefreshQueue contains the expected OrganisationProfileId and set to active
        assertOrganisationRefreshQueueEntitiesInDb("1", 1, true, true);
        assertOrganisationRefreshQueueEntitiesInDb("2", 1, true, true);
        assertOrganisationRefreshQueueEntitiesInDb("3", 1, true, true);
    }

    private void runTest(List<String> jurisdictionFileNames) {

        // GIVEN
        logBeforeStatus();
        // stub the PRD service call with response for test scenario
        stubPrdRetrieveOrganisations(jurisdictionFileNames);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();

        // THEN
        //verifySingleCallToPrd();
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports success
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus(),
            "Invalid process monitor end status");
    }

    //#region Assertion Helpers: DB Checks

    private void assertTotalProfileRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var profileRefreshQueueEntities = profileRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, profileRefreshQueueEntities.size(),
            "ProfileRefreshQueueEntity number of records mismatch");
    }

    private void assertTotalOrganisationRefreshQueueEntitiesInDb(int expectedNumberOfRecords) {
        var organisationRefreshQueueEntities = organisationRefreshQueueRepository.findAll();
        assertEquals(expectedNumberOfRecords, organisationRefreshQueueEntities.size(),
            "OrganisationRefreshQueueEntity number of records mismatch");
    }

    private void assertProfileRefreshQueueEntityInDb(String organisationProfileId,
        int expectedAccessTypesMinVersion, boolean expectedActive) {
        var profileRefreshQueueEntity = profileRefreshQueueRepository.findById(organisationProfileId);
        assertTrue(profileRefreshQueueEntity.isPresent(), "ProfileRefreshQueueEntity not found");
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "ProfileRefreshQueueEntity.AccessTypesMinVersion mismatch");
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive(),
            "ProfileRefreshQueueEntity.Active status mismatch");
    }

    private void assertOrganisationRefreshQueueEntitiesInDb(String organisationIdentifierId,
        int expectedAccessTypesMinVersion,
        boolean expectedActive,
        boolean expectedOrganisationLastUpdatedNow) {
        var profileRefreshQueueEntity = organisationRefreshQueueRepository.findById(organisationIdentifierId);
        assertTrue(profileRefreshQueueEntity.isPresent(), "OrganisationRefreshQueueEntity not found");
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion(),
            "OrganisationRefreshQueueEntity.AccessTypesMinVersion mismatch");
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive(),
            "OrganisationRefreshQueueEntity.Active status mismatch");
        log.info("OrganisationRefreshQueueEntity.OrganisationLastUpdated: {}",
            profileRefreshQueueEntity.get().getOrganisationLastUpdated());
        assertEquals(expectedOrganisationLastUpdatedNow,
            assertLastUpdatedNow(profileRefreshQueueEntity.get().getLastUpdated()),
            "OrganisationRefreshQueueEntity.LastUpdated mismatch");
    }

    private boolean assertLastUpdatedNow(LocalDateTime lastUpdated) {
        return lastUpdated.isAfter(LocalDateTime.now().minusMinutes(1));
    }

    //#endregion


    //#region Assertion Helpers: AccessTypes.SolicitorProfile

    private void assertCivilSolicitorProfile(OrganisationProfileJurisdiction solicitorProfile,
                                             String scenarioId,
                                             List<String> expectedAccessTypeIds) {

        assertEquals(JURISDICTION_ID_CIVIL, solicitorProfile.getJurisdictionId());

        var organisationProfileAccessTypes = extractJurisdictionsOrganisationProfileAccessTypes(solicitorProfile);
        assertEquals(expectedAccessTypeIds.size(), organisationProfileAccessTypes.size());
        assertTrue(organisationProfileAccessTypes.keySet().containsAll(expectedAccessTypeIds),
            "Expected access types not found in organisation profile access types"
        );

        switch (scenarioId) {
            case "jurisdiction_civil_scenario_01" -> {
                var accessType = organisationProfileAccessTypes.get(CIVIL_SOLICITOR_1);
                assertTrue(accessType.isAccessMandatory());
                assertTrue(accessType.isAccessDefault());

                var roles = accessType.getRoles().stream().toList();
                assertEquals(1, roles.size());

                assertEquals("civil_case_type_1", roles.get(0).getCaseTypeId());
                assertEquals("orgRole1", roles.get(0).getOrganisationalRoleName());
                assertEquals("groupRole1", roles.get(0).getGroupRoleName());
                assertEquals("CIVIL:$ORGID$", roles.get(0).getCaseGroupIdTemplate());
                assertTrue(roles.get(0).isGroupAccessEnabled());
            }

            case "jurisdiction_civil_scenario_02" -> {
                var accessType = organisationProfileAccessTypes.get(CIVIL_SOLICITOR_0);
                assertFalse(accessType.isAccessMandatory());
                assertFalse(accessType.isAccessDefault());

                var roles = accessType.getRoles().stream().toList();
                assertEquals(1, roles.size());

                assertEquals("civil_case_type_0", roles.get(0).getCaseTypeId());
                assertEquals("orgRole1", roles.get(0).getOrganisationalRoleName());
                assertEquals("groupRole1", roles.get(0).getGroupRoleName());
                assertEquals("CIVIL:$ORGID$", roles.get(0).getCaseGroupIdTemplate());
                assertTrue(roles.get(0).isGroupAccessEnabled());
            }

            default -> fail("Invalid scenario_id: " + scenarioId);
        }

    }

    private void assertPublicLawSolicitorProfile(OrganisationProfileJurisdiction solicitorProfile,
                                                 String scenarioId,
                                                 List<String> expectedAccessTypeIds) {

        assertEquals(JURISDICTION_ID_PUBLICLAW, solicitorProfile.getJurisdictionId());

        var organisationProfileAccessTypes = extractJurisdictionsOrganisationProfileAccessTypes(solicitorProfile);
        assertEquals(expectedAccessTypeIds.size(), organisationProfileAccessTypes.size());
        assertTrue(organisationProfileAccessTypes.keySet().containsAll(expectedAccessTypeIds),
            "Expected access types not found in organisation profile access types"
        );

        switch (scenarioId) {
            case "jurisdiction_publiclaw_scenario_01" -> {
                var accessType = organisationProfileAccessTypes.get(PUBLICLAW_SOLICITOR_1);
                assertTrue(accessType.isAccessMandatory());
                assertTrue(accessType.isAccessDefault());

                var roles = accessType.getRoles().stream().toList();
                assertEquals(1, roles.size());

                assertEquals("publiclaw_case_type_1", roles.get(0).getCaseTypeId());
                assertEquals("orgRole1", roles.get(0).getOrganisationalRoleName());
                assertEquals("groupRole1", roles.get(0).getGroupRoleName());
                assertEquals("PUBLICLAW:$ORGID$", roles.get(0).getCaseGroupIdTemplate());
                assertTrue(roles.get(0).isGroupAccessEnabled());
            }

            case "jurisdiction_publiclaw_scenario_02" -> {
                var accessType = organisationProfileAccessTypes.get(PUBLICLAW_SOLICITOR_2);
                assertTrue(accessType.isAccessMandatory());
                assertTrue(accessType.isAccessDefault());

                var roles = accessType.getRoles().stream().toList();
                assertEquals(2, roles.size());

                assertEquals("publiclaw_case_type_1", roles.get(0).getCaseTypeId());
                assertEquals("publiclaw_case_type_2", roles.get(1).getCaseTypeId());
                assertEquals("orgRole1", roles.get(0).getOrganisationalRoleName());
                assertEquals("orgRole1", roles.get(1).getOrganisationalRoleName());
                assertEquals("groupRole1", roles.get(0).getGroupRoleName());
                assertEquals("groupRole1", roles.get(1).getGroupRoleName());
                assertEquals("PUBLICLAW:$ORGID$", roles.get(0).getCaseGroupIdTemplate());
                assertEquals("PUBLICLAW:$ORGID$", roles.get(1).getCaseGroupIdTemplate());
                assertTrue(roles.get(0).isGroupAccessEnabled());
                assertTrue(roles.get(1).isGroupAccessEnabled());
            }

            default -> fail("Invalid scenario_id: " + scenarioId);
        }

    }

    //#endregion


    private OrganisationProfileJurisdiction extractJurisdictionsSolicitorProfileConfig(
        RestructuredAccessTypes accessTypes,
        String organisationProfileId,
        String jurisdiction
    ) {
        var organisationProfile = accessTypes.getOrganisationProfiles().stream()
            .filter(orgProfile -> orgProfile.getOrganisationProfileId().equals(organisationProfileId))
            .findFirst();
        assertTrue(organisationProfile.isPresent(), "Organisation Profile not found");

        var organisationProfileJurisdiction = organisationProfile.get().getJurisdictions().stream()
            .filter(jurisdictionObj -> jurisdictionObj.getJurisdictionId().equals(jurisdiction))
            .findFirst();
        assertTrue(organisationProfileJurisdiction.isPresent(), "Organisation Profile Jurisdiction not found");

        return organisationProfileJurisdiction.get();
    }

    private Map<String, OrganisationProfileAccessType> extractJurisdictionsOrganisationProfileAccessTypes(
        OrganisationProfileJurisdiction organisationProfileJurisdiction
    ) {
        return organisationProfileJurisdiction.getAccessTypes().stream()
            .collect(Collectors.toMap(OrganisationProfileAccessType::getAccessTypeId, accessType -> accessType));
    }


    private void logAfterStatus(ProcessMonitorDto processMonitorDto) {
        logObject("ProcessMonitorDto: AFTER", processMonitorDto);
        logObject("ProfileRefreshQueueRepository: AFTER", profileRefreshQueueRepository.getActiveProfileEntities());
        //logObject("AccessTypes: AFTER", accessTypesRepository.getAccessTypesEntity());
        //logOrganisationProfiles(accessTypesRepository.getAccessTypesEntity());

    }

    private void logBeforeStatus() {
        logObject("ProfileRefreshQueueRepository: BEFORE", profileRefreshQueueRepository.getActiveProfileEntities());
        //logObject("AccessTypes: BEFORE", accessTypesRepository.getAccessTypesEntity());
        //logOrganisationProfiles(accessTypesRepository.getAccessTypesEntity());

    }


    private void verifySingleCallToPrd() {
        var allCallEvents = logWiremockPostCalls(STUB_ID_PRD_RETRIEVE_ORGANISATIONS);
        // verify single call
        assertEquals(1, allCallEvents.size());
        var event = allCallEvents.get(0);
        // verify request headers contain Auth tokens
        var request = event.getRequest();
        assertTrue(request.getHeader(AUTHORIZATION).endsWith(DUMMY_AUTH_TOKEN));
        assertTrue(request.getHeader(SERVICE_AUTHORIZATION).endsWith(DUMMY_S2S_TOKEN));
        // verify response status
        assertEquals(HttpStatus.OK.value(), event.getResponse().getStatus());
    }

}
