package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class DroolPossessionsJudicialOfficeHolderMappingTest extends DroolBase {

    private static final String USER_ID = "3168da13-00b3-41e3-81fa-cbc71ac28a69";

    private static final ZonedDateTime BOOKING_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
    private static final ZonedDateTime BOOKING_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
    private static final String ACCESS_PROFILE_PRIMARY_LOCATION_ID = "London";
    private static final ZonedDateTime ACCESS_PROFILE_BEGIN_TIME = ZonedDateTime.now(ZoneOffset.UTC).minusMonths(1);
    private static final ZonedDateTime ACCESS_PROFILE_END_TIME = ZonedDateTime.now(ZoneOffset.UTC).plusMonths(1);
    private static final String BOOKING_REGION_ID = "1";
    private static final String BOOKING_LOCATION_ID = "Scotland";
    private static final String ACCESS_PROFILE_REGION_ID = "LDN";

    // NB: multi-regions are: London and South-East
    static List<String> multiRegionList = List.of("1", "5");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("hmcts-Judiciary", null);
        expectedRoleNameWorkTypesMap.put("Fee Paid-judge", null);
        expectedRoleNameWorkTypesMap.put("Judge", "hearing_work,routine_work,decision_making_work,"
                + "applications");
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("specific-access-approver-legal-ops", "access_requests");
        expectedRoleNameWorkTypesMap.put("circuit-judge", "appeals");
    }

    @ParameterizedTest
    @CsvSource({
            "Fee Paid,'','hmcts-judiciary,fee-paid-judge'",
            "Circuit Judge,'','hmcts-judiciary,fee-paid-judge'"
    })

    void verifyFeePaidRolesWithoutBooking(String appointment, String assignedRoles, String expectedRoleNames) {
        shouldReturnFeePaidRolesFromJudicialAccessProfile(appointment, false, assignedRoles, expectedRoleNames);
    }

    void shouldReturnFeePaidRolesFromJudicialAccessProfile(
            String appointment, boolean addBooking, String assignedRoles, String expectedRoleNames) {

        clearAndPrepareProfilesForDroolSession(
                appointment,
                "Fee-paid",
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
                        .ticketCodes(List.of("AAA3"))
                        .beginTime(ACCESS_PROFILE_BEGIN_TIME)
                        .endTime(ACCESS_PROFILE_END_TIME)
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("AAA3"))
                                        .jurisdiction("PCS")
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
            assertEquals("AAA3", r.getAuthorisations().get(0));
            assertEquals("PCS", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());

            if (r.getRoleName().equals("Fee Paid-judge") && appointmentType.equals("Fee Paid")) {
                assertEquals(ACCESS_PROFILE_BEGIN_TIME, r.getBeginTime());
                assertEquals(ACCESS_PROFILE_END_TIME.plusDays(1), r.getEndTime());
                assertEquals(ACCESS_PROFILE_PRIMARY_LOCATION_ID, primaryLocation);
                assertNull(r.getAttributes().get("region"));
            }  else {
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

    private List<FeatureFlag> setFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction("PCS", true));

        for (FeatureFlag flag : featureFlags) {
            if (flag.getFlagName().contains("hearing")) {
                flag.setStatus(true);
            }
        }

        return featureFlags;
    }
}