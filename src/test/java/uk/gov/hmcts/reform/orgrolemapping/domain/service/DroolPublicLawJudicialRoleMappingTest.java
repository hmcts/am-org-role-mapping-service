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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolPublicLawJudicialRoleMappingTest extends DroolBase {

    private static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    private static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    private static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
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
        "Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',2,false",

        "District Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "District Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "District Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',3,false",

        "District Judge (MC),Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "District Judge (MC),Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "District Judge (MC),Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',4,false",

        "High Court Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "High Court Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "High Court Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',6,false",

        "_,Salaried,true,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer,hearing-manager',1,true",
        "_,Salaried,true,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer,hearing-manager',5,true",
        "_,Salaried,true,'Designated Family Judge','leadership-judge,judge,task-supervisor,hmcts-judiciary,"
                + "case-allocator,specific-access-approver-judiciary,hearing-viewer,hearing-manager',7,false",

        "Tribunal Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "Tribunal Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "Tribunal Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',8,false",

        "Employment Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "Employment Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "Employment Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',9,false",

        "Specialist Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "Specialist Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "Specialist Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',10,false",

        "Senior Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',1,true",
        "Senior Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',5,true",
        "Senior Circuit Judge,Salaried,true,'','judge,hmcts-judiciary,hearing-viewer,hearing-manager',11,false",
    })
    void shouldReturnSalariedRolesFromJudicialAccessProfile(
            String appointment, String appointmentType, boolean hearingFlag, String assignedRoles,
            String expectedRoleNames, String region, boolean expectMultiRegion) {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(Arrays.stream(assignedRoles.split(",")).toList())
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
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags(hearingFlag));

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
            assertEquals(ActorIdType.IDAM, r.getActorIdType());
            assertEquals(USER_ID, r.getActorId());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);

            assertRoleSpecificAttributes(r, appointmentType, roleNameToRegionsMap, region);
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
        "Deputy Circuit Judge,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer,"
                + "hearing-manager',1,false",
        "Recorder,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer,hearing-manager',2,false",
        "Deputy District Judge - PRFD,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer,"
                + "hearing-manager',3,false",
        "Deputy District Judge (MC)- Fee paid,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',4,false",
        "Deputy District Judge (MC)- Sitting in Retirement,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',5,false",
        "Deputy District Judge- Fee-Paid,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer,"
                + "hearing-manager',6,false",
        "Deputy District Judge- Sitting in Retirement,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',7,false",
        "Deputy High Court Judge,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer"
                + ",hearing-manager',8,false",
        "High Court Judge- Sitting in Retirement,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',9,false",
        "Circuit Judge (sitting in retirement),Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',10,false",
        "Recorder (sitting in retirement),Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer"
                + ",hearing-manager',10,false",
        "Deputy Upper Tribunal Judge,Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,hearing-viewer,"
                + "hearing-manager',11,false",
        "District Judge (MC) (sitting in retirement),Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',12,false",
        "District Judge (sitting in retirement),Fee Paid,true,true,'','judge,fee-paid-judge,hmcts-judiciary,"
                + "hearing-viewer,hearing-manager',1,false",
    })
    void shouldReturnFeePaidRolesFromJudicialAccessProfile(
            String appointment, String appointmentType, boolean addBooking, boolean hearingFlag,
            String assignedRoles, String expectedRoleNames, String region, boolean expectMultiRegion) {

        allProfiles.clear();
        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        judicialBookings.clear();

        JudicialBooking booking = null;
        if (addBooking) {
            booking = JudicialBooking.builder()
                    .userId(USER_ID).locationId(BOOKING_LOCATION_ID)
                    .regionId(region)
                    .beginTime(BOOKING_BEGIN_TIME)
                    .endTime(BOOKING_END_TIME)
                    .build();
            judicialBookings.add(booking);
        }

        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(USER_ID)
                        .roles(Arrays.stream(assignedRoles.split(",")).toList())
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

        // create map for all fee paid roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "judge",
                "fee-paid-judge"
        );

        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags(hearingFlag));

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
            assertEquals(ActorIdType.IDAM, r.getActorIdType());
            assertEquals(USER_ID, r.getActorId());
            assertEquals(RoleType.ORGANISATION, r.getRoleType());
            assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
            if (r.getAttributes().get("contractType") != null) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            }

            String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
            String actualWorkTypes = null;
            if (r.getAttributes().get("workTypes") != null) {
                actualWorkTypes = r.getAttributes().get("workTypes").asText();
            }
            assertEquals(expectedWorkTypes, actualWorkTypes);

            assertRoleSpecificAttributes(r, appointmentType, roleNameToRegionsMap, region);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                region, // fallback if not multi-region scenario
                booking != null ? booking.getRegionId() : null
        );
    }

    private void assertRoleSpecificAttributes(RoleAssignment r, String appointmentType,
                                              Map<String, List<String>> roleNameToRegionsMap, String region) {
        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        // check region status and add to map
        MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);

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
                assertEquals(region, r.getAttributes().get("region").asText());
                assertEquals(BOOKING_LOCATION_ID, primaryLocation);
            } else {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
            }

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

    private List<FeatureFlag> setFeatureFlags(boolean hearingFlag) {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("PUBLICLAW", true));

        featureFlags.add(
                FeatureFlag.builder()
                        .flagName("sscs_hearing_1_0")
                        .status(hearingFlag)
                        .build()
        );

        return featureFlags;
    }
}
