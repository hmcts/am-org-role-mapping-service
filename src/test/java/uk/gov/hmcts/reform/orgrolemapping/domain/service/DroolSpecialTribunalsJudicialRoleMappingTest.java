package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.*;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class DroolSpecialTribunalsJudicialRoleMappingTest extends DroolBase {

    static Map<String, String> employmentExpectedRoleNameWorkTypesMap = new HashMap<>();

    {
        employmentExpectedRoleNameWorkTypesMap.put("senior-judge", null);
        employmentExpectedRoleNameWorkTypesMap.put("judge", "hearing_work,decision_making_work,routine_work,"
                + "applications,priority");
        employmentExpectedRoleNameWorkTypesMap.put("case-allocator", null);
        employmentExpectedRoleNameWorkTypesMap.put("task-supervisor", null);
        employmentExpectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        employmentExpectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        employmentExpectedRoleNameWorkTypesMap.put("medical", "hearing_work,decision_making_work,"
                + "routine_work,priority");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-medical", "hearing_work,decision_making_work,"
                + "routine_work,applications,priority");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-disability", "hearing_work,priority");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-financial", "hearing_work,priority");
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

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertTrue(r.isReadOnly());
            assertNull(primaryLocation);
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("ST_CIC", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
            assertEquals("2", primaryLocation);
        }

        //region assertions
        assertNull(r.getAttributes().get("region"));

        //work types assertions
        if (office.equalsIgnoreCase("SPECIALTRIBUNALS Tribunal Member-Fee Paid") && r.getRoleName().equalsIgnoreCase("fee-paid-tribunal-member")) {
            employmentExpectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,decision_making_work,routine_work,applications,priority");
        } else if (office.equalsIgnoreCase("SPECIALTRIBUNALS Tribunal Member Lay-Fee Paid") && r.getRoleName().equalsIgnoreCase("fee-paid-tribunal-member")) {
            employmentExpectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,priority");
        }
        String expectedWorkTypes = employmentExpectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    @ParameterizedTest
    @CsvSource({
            "SPECIALTRIBUNALS President of Tribunal-Salaried,'senior-judge,judge,case-allocator,task-supervisor,hmcts-judiciary,specific-access-approver-judiciary'",
            "SPECIALTRIBUNALS Tribunal Judge-Salaried,'judge,case-allocator,task-supervisor,hmcts-judiciary,specific-access-approver-judiciary'",
            "SPECIALTRIBUNALS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary'"
    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("specialtribunals_wa_1_0", true));

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
            "SPECIALTRIBUNALS Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
            "SPECIALTRIBUNALS Tribunal Member-Fee Paid,'fee-paid-tribunal-member'",
            "SPECIALTRIBUNALS Tribunal Member Medical-Fee Paid,'fee-paid-medical'",
            "SPECIALTRIBUNALS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member'",
            "SPECIALTRIBUNALS Tribunal Member Disability-Fee Paid,'fee-paid-disability'",
            "SPECIALTRIBUNALS Tribunal Member Financially Qualified-Fee Paid,'fee-paid-financial'"
    })
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("specialtribunals_wa_1_0", true));

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
            "SPECIALTRIBUNALS Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'"
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
                buildExecuteKieSession(getFeatureFlags("specialtribunals_wa_1_0", true));

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
