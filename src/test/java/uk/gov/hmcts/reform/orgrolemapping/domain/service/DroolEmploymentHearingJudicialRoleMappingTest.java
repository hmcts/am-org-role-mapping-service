package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class DroolEmploymentHearingJudicialRoleMappingTest extends DroolBase {

    static String userId = "3168da13-00b3-41e3-81fa-cbc71ac28a69";
    List<String> judgeRoleNamesWithWorkTypes = List.of("judge", "fee-paid-judge");

    static Map<String, String> employmentExpectedRoleNameWorkTypesMap = new HashMap<>();

    {
        employmentExpectedRoleNameWorkTypesMap.put("leadership-judge", null);
        employmentExpectedRoleNameWorkTypesMap.put("judge", "hearing_work,decision_making_work,routine_work,"
                + "applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put("task-supervisor", null);
        employmentExpectedRoleNameWorkTypesMap.put("case-allocator", null);
        employmentExpectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        employmentExpectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,decision_making_work,routine_work,"
                + "applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put("tribunal-member", "hearing_work");
    }

    static Stream<Arguments> endToEndData() {
        return Stream.of(
                Arguments.of("President of Tribunal",
                        "Salaried",
                        false,
                        true,
                        List.of("President of Tribunal"),
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                Arguments.of("Vice President",
                        "Salaried",
                        false,
                        true,
                        List.of("Vice President"),
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                Arguments.of("Regional Employment Judge",
                        "Salaried",
                        false,
                        true,
                        List.of("Regional Employment Judge"),
                        List.of("leadership-judge", "judge", "task-supervisor", "case-allocator", "hmcts-judiciary",
                                "specific-access-approver-judiciary", "hearing-viewer"),
                        null),
                Arguments.of("Employment Judge",
                        "Salaried",
                        false,
                        true,
                        List.of("Employment Judge"),
                        List.of("judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                Arguments.of("Employment Judge",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Employment Judge"),
                        List.of("fee-paid-judge", "hmcts-judiciary", "hearing-viewer"),
                        null),
                //Tribunal Member and Lay should get roles tribunal-member,hearing-viewer when baseLocationId = 1036
                // or 1037
                Arguments.of("Tribunal Member",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member"),
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1036"),
                Arguments.of("Tribunal Member Lay",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member Lay"),
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1036"),
                Arguments.of("Tribunal Member",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member"),
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1037"),
                Arguments.of("Tribunal Member Lay",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member Lay"),
                        List.of("tribunal-member", "hmcts-judiciary", "hearing-viewer"),
                        "1037"),
                //Tribunal Member and Lay should NOT get roles when baseLocationId != 1036 or 1037
                Arguments.of("Tribunal Member",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member"),
                        new ArrayList<>(),
                        "1"),
                Arguments.of("Tribunal Member Lay",
                        "Fee Paid",
                        false,
                        true,
                        List.of("Tribunal Member Lay"),
                        new ArrayList<>(),
                        "1")
        );
    }

    @ParameterizedTest
    @MethodSource("endToEndData")
    void shouldTakeJudicialAccessProfileConvertToJudicialOfficeHolderThenReturnRoleAssignments(
            String appointment, String appointmentType, boolean addBooking, boolean hearingFlag,
            List<String> assignedRoles, List<String> expectedRoleNames, String baseLocationId) {

        judicialAccessProfiles.clear();
        judicialOfficeHolders.clear();
        judicialBookings.clear();
        if (addBooking) {
            JudicialBooking booking = JudicialBooking.builder()
                    .userId(userId).locationId("Scotland").regionId("1")
                    .build();
            judicialBookings.add(booking);
        }
        judicialAccessProfiles.add(
                JudicialAccessProfile.builder()
                        .appointment(appointment)
                        .appointmentType(appointmentType)
                        .userId(userId)
                        .roles(assignedRoles)
                        .regionId("LDN")
                        .baseLocationId(baseLocationId)
                        .primaryLocationId("London")
                        .ticketCodes(List.of("BHA1"))
                        .authorisations(List.of(
                                Authorisation.builder()
                                        .serviceCodes(List.of("BHA1"))
                                        .jurisdiction("EMPLOYMENT")
                                        .endDate(LocalDateTime.now().plusYears(1L))
                                        .build()
                        ))
                        .build()
        );

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(
                        List.of(FeatureFlag.builder().flagName("employment_wa_1_0").status(true).build(),
                                FeatureFlag.builder().flagName("sscs_hearing_1_0").status(hearingFlag).build())
                );

        List<String> roleNameResults =
                roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList());
        assertThat(roleNameResults, containsInAnyOrder(expectedRoleNames.toArray()));

        String regionId = allProfiles.iterator().next().getRegionId();
        //assertions
        roleAssignments.forEach(r -> {
            assertEquals(userId, r.getActorId());
            assertCommonRoleAssignmentAttributes(r, "LDN", appointment);
        });

    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String regionId, String office) {


        //filter

        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(userId, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
        assertEquals(null, r.getAttributes().get("bookable"));

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(null, r.getAttributes().get("region"));
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertEquals(true, r.isReadOnly());
        } else if (r.getRoleName().equals("hearing-viewer")) {
            assertEquals(null, r.getAttributes().get("region"));
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
            assertEquals(false, r.isReadOnly());
            assertEquals(null, r.getAttributes().get("contractType"));
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
            assertEquals(false, r.isReadOnly());
        }

        String expectedWorkTypes = employmentExpectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }
}
