package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertHelper.MultiRegion;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPublicLawJudicialRoleMappingTest extends DroolBase {

    private static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    private static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    private static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
    private static final String BOOKING_REGION_ID = "1";
    private static final String BOOKING_LOCATION_ID = "Scotland";
    private static final ZonedDateTime ACCESS_PROFILE_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1);
    private static final ZonedDateTime ACCESS_PROFILE_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1);
    private static final String ACCESS_PROFILE_REGION_ID = "LDN";
    private static final String ACCESS_PROFILE_PRIMARY_LOCATION_ID = "London";

    // NB: multi-regions are: London and South-East
    static List<String> multiRegionList = List.of("1", "5");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("judge", "hearing_work,routine_work,decision_making_work,"
                + "applications");
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("leadership-judge", "hearing_work,decision_making_work,applications,"
                + "access_requests");
        expectedRoleNameWorkTypesMap.put("task-supervisor", "hearing_work,routine_work,decision_making_work,"
                + "applications,access_requests");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,routine_work,"
                + "decision_making_work,applications");
        expectedRoleNameWorkTypesMap.put("magistrate", null);
    }

    @ParameterizedTest
    @CsvSource({
        "Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',2,false",

        "District Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "District Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "District Judge,'','judge,hmcts-judiciary,hearing-viewer',3,false",

        "District Judge (MC),'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "District Judge (MC),'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "District Judge (MC),'','judge,hmcts-judiciary,hearing-viewer',4,false",

        "High Court Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "High Court Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "High Court Judge,'','judge,hmcts-judiciary,hearing-viewer',6,false",

        "_,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer',1,true",
        "_,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer',5,true",
        "_,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer',7,false",

        "Tribunal Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "Tribunal Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "Tribunal Judge,'','judge,hmcts-judiciary,hearing-viewer',11,false",

        "Employment Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "Employment Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "Employment Judge,'','judge,hmcts-judiciary,hearing-viewer',11,false",

        "Specialist Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "Specialist Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "Specialist Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',11,false",

        "Senior Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',1,true",
        "Senior Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',5,true",
        "Senior Circuit Judge,'','judge,hmcts-judiciary,hearing-viewer',11,false"
    })
    void verifySalariedAndSptwRoles(String appointment, String assignedRoles, String expectedRoleNames,
                                    String region, boolean expectMultiRegion) {
        shouldReturnSalariedRolesFromJudicialAccessProfile(
                appointment, "Salaried", assignedRoles, expectedRoleNames, region, expectMultiRegion);
        shouldReturnSalariedRolesFromJudicialAccessProfile(
                appointment, "SPTW", assignedRoles, expectedRoleNames, region, expectMultiRegion);
    }

    void shouldReturnSalariedRolesFromJudicialAccessProfile(
            String appointment, String appointmentType, String assignedRoles,
            String expectedRoleNames, String region, boolean expectMultiRegion) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                appointmentType,
                Arrays.stream(assignedRoles.split(",")).toList(),
                region,
                false
        );

        // create map for all salaried roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "judge",
                "leadership-judge",
                "task-supervisor",
                "case-allocator",
                "specific-access-approver-judiciary"
        );

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertions
        List<String> expectedRoleList = Arrays.stream(expectedRoleNames.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            assertRoleSpecificAttributes(r, "Salaried", roleNameToRegionsMap);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                region, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Magistrate,'','magistrate,hearing-viewer'"
    })
    void shouldReturnVoluntaryRolesFromJudicialAccessProfile(
            String appointment, String assignedRoles, String expectedRoleNames) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                "Voluntary",
                Arrays.stream(assignedRoles.split(",")).toList(),
                ACCESS_PROFILE_REGION_ID,
                false
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertions
        assertFalse(roleAssignments.isEmpty());
        assertEquals(expectedRoleNames.split(",").length, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoleNames.split(",")));

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Voluntary", r.getAttributes().get("contractType").asText());
            }

            assertRoleSpecificAttributes(r, "Voluntary", null);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "Deputy Circuit Judge,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Recorder,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge - PRFD,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge (MC)- Fee paid,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge (MC)- Sitting in Retirement,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge- Fee-Paid,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge- Sitting in Retirement,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy High Court Judge,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "High Court Judge- Sitting in Retirement,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Circuit Judge (sitting in retirement),'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Recorder (sitting in retirement),'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy Upper Tribunal Judge,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "District Judge (MC) (sitting in retirement),'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "District Judge (sitting in retirement),'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer'"
    })
    void verifyFeePaidRolesWithBooking(String appointment, String assignedRoles, String expectedRoleNames) {
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, true, assignedRoles, expectedRoleNames);
    }

    @ParameterizedTest
    @CsvSource({
        "Deputy Circuit Judge,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Recorder,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge - PRFD,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge (MC)- Fee paid,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge (MC)- Sitting in Retirement,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge- Fee-Paid,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy District Judge- Sitting in Retirement,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy High Court Judge,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "High Court Judge- Sitting in Retirement,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Circuit Judge (sitting in retirement),'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Recorder (sitting in retirement),'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "Deputy Upper Tribunal Judge,'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "District Judge (MC) (sitting in retirement),'','fee-paid-judge,hmcts-judiciary,hearing-viewer'",
        "District Judge (sitting in retirement),'','fee-paid-judge,hmcts-judiciary,hearing-viewer'"
    })
    void verifyFeePaidRolesWithoutBooking(String appointment, String assignedRoles, String expectedRoleNames) {
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, false, assignedRoles, expectedRoleNames);
    }

    void shouldReturnFeePaidRolesFromJudicialAccessProfile(
            String appointment, boolean addBooking, String assignedRoles, String expectedRoleNames) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                "Fee Paid",
                Arrays.stream(assignedRoles.split(",")).toList(),
                ACCESS_PROFILE_REGION_ID,
                addBooking
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertions
        assertFalse(roleAssignments.isEmpty());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoleNames.split(",")));
        assertEquals(expectedRoleNames.split(",").length, roleAssignments.size());

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            }

            assertRoleSpecificAttributes(r, "Fee Paid", null);
        });
    }

    private void assertRoleSpecificAttributes(RoleAssignment r, String appointmentType,
                                              Map<String, List<String>> roleNameToRegionsMap) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(USER_ID, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (roleNameToRegionsMap != null) {
            // check region status and add to map
            MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);
        }

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertTrue(r.isReadOnly());
            assertNull(r.getAttributes().get("jurisdiction"));
            assertNull(primaryLocation);
            assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
            assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("ABA3", r.getAuthorisations().get(0));
            assertEquals("PUBLICLAW", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());

            if (r.getRoleName().equals("judge") && appointmentType.equals("Fee Paid")) {
                assertEquals(BOOKING_BEGIN_TIME, r.getBeginTime());
                assertEquals(BOOKING_END_TIME, r.getEndTime());
                assertEquals(BOOKING_REGION_ID, r.getAttributes().get("region").asText());
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
            } else {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                if (!r.getRoleName().equals("hearing-viewer")
                        && !r.getRoleName().equals("hearing-manager") && roleNameToRegionsMap == null) {
                    assertEquals(ACCESS_PROFILE_REGION_ID, r.getAttributes().get("region").asText());
                }
            }

        }
    }

    private void clearAndPrepareProfilesForDroolSession(String appointment, String appointmentType, List<String> roles,
                                                        String region, boolean addBooking) {
        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        judicialBookings.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(roles)
                        .regionId(region)
                        .primaryLocationId(ACCESS_PROFILE_PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of("ABA3"))
                        .beginTime(ACCESS_PROFILE_BEGIN_TIME)
                        .endTime(ACCESS_PROFILE_END_TIME)
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("ABA3"))
                                        .jurisdiction("PUBLICLAW")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        if (addBooking) {
            judicialBookings.add(
                    JudicialBooking.builder()
                            .userId(USER_ID).locationId(BOOKING_LOCATION_ID)
                            .regionId(BOOKING_REGION_ID)
                            .beginTime(BOOKING_BEGIN_TIME)
                            .endTime(BOOKING_END_TIME)
                            .build()
            );
        }
    }

    @Test
    void falsePublicLawFlagTest() {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment("District Judge (MC)")
                        .appointmentType("SPTW")
                        .userId(USER_ID)
                        .roles(List.of("District Judge"))
                        .regionId(ACCESS_PROFILE_REGION_ID)
                        .primaryLocationId(ACCESS_PROFILE_PRIMARY_LOCATION_ID)
                        .ticketCodes(List.of("ABA3"))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("ABA3"))
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("publiclaw_wa_1_0", false));

        //assertions
        assertTrue(roleAssignments.isEmpty());
    }

    private List<FeatureFlag> setFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("PUBLICLAW", true));

        for (FeatureFlag flag : featureFlags) {
            if (flag.getFlagName().contains("hearing")) {
                flag.setStatus(true);
            }
        }

        return featureFlags;
    }
}
