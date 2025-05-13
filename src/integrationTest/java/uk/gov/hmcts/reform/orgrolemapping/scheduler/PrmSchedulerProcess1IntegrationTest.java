package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

class PrmSchedulerProcess1IntegrationTest extends BaseSchedulerTestIntegration {

    private static final String CIVIL_SOLICITOR_0 = "CIVIL_SOLICITOR_0";
    private static final String CIVIL_SOLICITOR_1 = "CIVIL_SOLICITOR_1";
    private static final String PUBLICLAW_SOLICITOR_1 = "PUBLICLAW_SOLICITOR_1";
    private static final String PUBLICLAW_SOLICITOR_2 = "PUBLICLAW_SOLICITOR_2";

    @Autowired
    private AccessTypesRepository accessTypesRepository;

    @Autowired
    private ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    private Scheduler prmScheduler;

    /**
     * No change - Empty access types with no new access types.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNoChange_NoExistingProfiles() {

        // verify that the Access Types are updated (i.e. version 1) and empty
        runTest(List.of(), 1, 0);
    }

    /**
     * No change - No update to an already populated list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNoChange_ExistingAccessType() {

        int expectedAccessTypesMinVersion = 2;

        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_civil_scenario_02.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for civil 01
        assertCivilSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_CIVIL),
            "jurisdiction_civil_scenario_02",
            List.of(CIVIL_SOLICITOR_0)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    /**
     * New Casetype - Insert a single access type to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNewCaseType_NoExistingProfiles() {

        int expectedAccessTypesMinVersion = 1;

        // verify that the Access Types are updated (i.e. version 1) and has 1 organisation profile
        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_civil_scenario_01.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for civil 01
        assertCivilSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_CIVIL),
            "jurisdiction_civil_scenario_01",
            List.of(CIVIL_SOLICITOR_1)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    /**
     * New Casetype - Insert access types to an existing list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNewCaseType_ExistingProfile() {

        int expectedAccessTypesMinVersion = 2;

        // verify that the Access Types are updated (i.e. version 1) and has 1 organisation profile
        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_civil_scenario_01.json",
            "/SchedulerTests/CcdAccessTypes/jurisdiction_publiclaw_scenario_02.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for civil 01
        assertCivilSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_CIVIL),
            "jurisdiction_civil_scenario_01",
            List.of(CIVIL_SOLICITOR_1)
        );
        assertPublicLawSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_PUBLICLAW),
            "jurisdiction_publiclaw_scenario_02",
            List.of(PUBLICLAW_SOLICITOR_2)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    /**
     * New Org Profile - Insert multiple access types to an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/init_access_types.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNewOrg_NoExistingProfiles() {

        int expectedAccessTypesMinVersion = 1;

        // verify that the Access Types are updated (i.e. version 1) and has 1 organisation profile
        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_civil_scenario_01.json",
            "/SchedulerTests/CcdAccessTypes/jurisdiction_publiclaw_scenario_01.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for civil 01 & publiclaw 01
        assertCivilSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_CIVIL),
            "jurisdiction_civil_scenario_01",
            List.of(CIVIL_SOLICITOR_1)
        );
        assertPublicLawSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_PUBLICLAW),
            "jurisdiction_publiclaw_scenario_01",
            List.of(PUBLICLAW_SOLICITOR_1)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    /**
     * Existing Org Profile Updated - Insert a single access type to an already populated list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testNewOrg_ExistingProfile() {

        int expectedAccessTypesMinVersion = 2;

        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_civil_scenario_01.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for civil 01
        assertCivilSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_CIVIL),
            "jurisdiction_civil_scenario_01",
            List.of(CIVIL_SOLICITOR_1)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    /**
     * Existing Org Profile Deleted - Delete a single access type leaving an empty list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testDeleteOrgProfile_NoProfileRemains() {

        runTest(List.of(), 2, 0);
    }

    /**
     * Existing Org Profile Deleted - Delete a single access type leaving a populated list.
     */
    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/prm/access_types/insert_civil_access_type.sql",
        "classpath:sql/prm/profile_refresh_queue/init_profile_refresh_queue.sql"
    })
    void testDeleteOrgProfile_ExistingProfileRemains() {

        int expectedAccessTypesMinVersion = 2;

        // verify that the Access Types are updated (i.e. version 1) and has 1 organisation profile
        var accessTypes = runTest(List.of(
            "/SchedulerTests/CcdAccessTypes/jurisdiction_publiclaw_scenario_02.json"
        ), expectedAccessTypesMinVersion, 1);

        // verify that the OrganisationProfileId is as expected for publiclaw 01
        assertPublicLawSolicitorProfile(
            extractJurisdictionsSolicitorProfileConfig(accessTypes, SOLICITOR_PROFILE, JURISDICTION_ID_PUBLICLAW),
            "jurisdiction_publiclaw_scenario_02",
            List.of(PUBLICLAW_SOLICITOR_2)
        );

        // verify that the ProfileRefreshQueue contains the expected OrganisationProfileId
        assertProfileRefreshQueueEntityInDb(SOLICITOR_PROFILE, expectedAccessTypesMinVersion, true);
    }

    private RestructuredAccessTypes runTest(List<String> jurisdictionFileNames, int expectedVersion,
        int expectedOrganisationProfileCount) {

        // GIVEN
        logBeforeStatus();
        // stub the CCD Def Store call with response for test scenario
        stubCcdRetrieveAccessTypes(jurisdictionFileNames);

        // WHEN
        ProcessMonitorDto processMonitorDto = prmScheduler.findAndUpdateCaseDefinitionChanges();

        // THEN
        verifySingleCallToCcd();
        logAfterStatus(processMonitorDto);

        // verify that the process monitor reports success
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());

        // verify that the Access Types are updated (i.e. version 1) and has x organisation profile(s)
        var accessTypes = assertAndExtractAccessTypesFromDb(expectedVersion,
            expectedOrganisationProfileCount);

        // verify that the ProfileRefreshQueue now has x active entries
        assertActiveProfileRefreshQueueEntitiesInDb(expectedOrganisationProfileCount);

        return accessTypes;
    }

    //#region Assertion Helpers: DB Checks

    private RestructuredAccessTypes assertAndExtractAccessTypesFromDb(int expectedVersion,
                                                                      int expectedOrganisationProfileCount) {
        AccessTypesEntity accessTypesEntity = accessTypesRepository.getAccessTypesEntity();
        assertEquals(expectedVersion, accessTypesRepository.getAccessTypesEntity().getVersion());

        RestructuredAccessTypes accessTypes = extractAccessTypes(accessTypesEntity);
        assertEquals(expectedOrganisationProfileCount, accessTypes.getOrganisationProfiles().size());

        return accessTypes;
    }


    private void assertActiveProfileRefreshQueueEntitiesInDb(int expectedCount) {
        var activeProfileEntities = profileRefreshQueueRepository.getActiveProfileEntities();
        assertEquals(expectedCount, activeProfileEntities.size());
    }

    private void assertProfileRefreshQueueEntityInDb(String organisationProfileId,
                                                     int expectedAccessTypesMinVersion,
                                                     boolean expectedActive) {
        var profileRefreshQueueEntity = profileRefreshQueueRepository.findById(organisationProfileId);
        assertTrue(profileRefreshQueueEntity.isPresent());
        assertEquals(expectedAccessTypesMinVersion, profileRefreshQueueEntity.get().getAccessTypesMinVersion());
        assertEquals(expectedActive, profileRefreshQueueEntity.get().getActive());
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
        logObject("AccessTypes: AFTER", accessTypesRepository.getAccessTypesEntity());
        logOrganisationProfiles(accessTypesRepository.getAccessTypesEntity());
        logObject("ProfileRefreshQueueRepository: AFTER", profileRefreshQueueRepository.getActiveProfileEntities());
    }

    private void logBeforeStatus() {
        logObject("AccessTypes: BEFORE", accessTypesRepository.getAccessTypesEntity());
        logOrganisationProfiles(accessTypesRepository.getAccessTypesEntity());
        logObject("ProfileRefreshQueueRepository: BEFORE", profileRefreshQueueRepository.getActiveProfileEntities());
    }


    private void verifySingleCallToCcd() {
        var allCallEvents = logWiremockPostCalls(STUB_ID_CCD_RETRIEVE_ACCESS_TYPES);
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
