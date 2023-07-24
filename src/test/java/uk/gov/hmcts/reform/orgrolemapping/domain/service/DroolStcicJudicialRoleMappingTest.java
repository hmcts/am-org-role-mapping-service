package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DroolStcicJudicialRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    {
        expectedRoleNameWorkTypesMap.put("senior-judge", null);
        expectedRoleNameWorkTypesMap.put("judge", "hearing_work,decision_making_work,routine_work,"
                + "applications,priority");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        expectedRoleNameWorkTypesMap.put("medical", "hearing_work,decision_making_work,"
                + "routine_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-medical", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-disability", "hearing_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-financial", "hearing_work,priority");
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String office) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(TestDataBuilder.id_2, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
        assertNull(r.getAttributes().get("bookable"));

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        assertNull(primaryLocation);

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertTrue(r.isReadOnly());
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
        }

        //region assertions
        assertNull(r.getAttributes().get("region"));

        //work types assertions
        if (office.equals("ST_CIC Tribunal Member-Fee Paid")
                && r.getRoleName().equals("fee-paid-tribunal-member")) {
            expectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member",
                    "hearing_work,decision_making_work,routine_work,applications,priority");
        } else if (office.equals("ST_CIC Tribunal Member Lay-Fee Paid")
                && r.getRoleName().equals("fee-paid-tribunal-member")) {
            expectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,priority");
        }
        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    @ParameterizedTest
    @CsvSource({
            "ST_CIC President of Tribunal-Salaried,'senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "ST_CIC Principal Judge-Salaried,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                    + "hmcts-judiciary,specific-access-approver-judiciary'",
            "ST_CIC Tribunal Judge-Salaried,'judge,case-allocator,task-supervisor,hmcts-judiciary,"
                    + "specific-access-approver-judiciary'",
            "ST_CIC Tribunal Member Medical-Salaried,'medical,hmcts-judiciary'"
    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertNull(r.getAttributes().get("region"));
            assertCommonRoleAssignmentAttributes(r, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "ST_CIC Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
            "ST_CIC Tribunal Member-Fee Paid,'fee-paid-tribunal-member'",
            "ST_CIC Tribunal Member Medical-Fee Paid,'fee-paid-medical'",
            "ST_CIC Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member'",
            "ST_CIC Tribunal Member Disability-Fee Paid,'fee-paid-disability'",
            "ST_CIC Tribunal Member Financially Qualified-Fee Paid,'fee-paid-financial'"
    })
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertNull(r.getAttributes().get("region"));
            assertCommonRoleAssignmentAttributes(r, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "ST_CIC Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'"
    })
    void shouldReturnFeePaidRolesWithBooking(String setOffice, String expectedRoles) throws IOException {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("2");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertNull(r.getAttributes().get("region"));
            assertCommonRoleAssignmentAttributes(r, setOffice);
        });
    }
}
