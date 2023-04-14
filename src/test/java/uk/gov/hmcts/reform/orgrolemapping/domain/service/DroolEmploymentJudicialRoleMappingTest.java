package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolEmploymentJudicialRoleMappingTest extends DroolBase {
    static Map<String, String> employmentExpectedRoleNameWorkTypesMap = new HashMap<>();

    {
        employmentExpectedRoleNameWorkTypesMap.put("leadership-judge", null);
        employmentExpectedRoleNameWorkTypesMap.put("judge", "hearing_work,decision_making_work,routine_work,"
                + "applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put("task-supervisor", null);
        employmentExpectedRoleNameWorkTypesMap.put("case-allocator", null);
        employmentExpectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        employmentExpectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        employmentExpectedRoleNameWorkTypesMap.put("fee-paid-judge", "hearing_work,decision_making_work,"
                + "routine_work,applications,amendments");
        employmentExpectedRoleNameWorkTypesMap.put("tribunal-member", "hearing_work");
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String regionId, String office) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(TestDataBuilder.id_2, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

        String primaryLocation = null;
        if (r.getAttributes().get("primaryLocation") != null) {
            primaryLocation = r.getAttributes().get("primaryLocation").asText();
        }

        if (r.getRoleName().equals("hmcts-judiciary")) {
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
            assertEquals(true, r.isReadOnly());
            assertEquals(null, primaryLocation);
        } else {
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
            assertEquals(false, r.isReadOnly());
            assertEquals("2", primaryLocation);
        }

        //region assertions
        if (List.of("leadership-judge", "judge", "specific-access-approver-judiciary").contains(r.getRoleName())
                && !office.contains("President of Tribunal")) {
            assertEquals(regionId, r.getAttributes().get("region").asText());
        } else {
            assertEquals(null, r.getAttributes().get("region"));
        }

        if ((r.getRoleName().equals("hmcts-judiciary") && office.equals("EMPLOYMENT Employment Judge-Fee-Paid"))
            || (r.getRoleName().equals("hmcts-judiciary") && office.equals("EMPLOYMENT Tribunal Member-Fee-Paid"))
            || (r.getRoleName().equals("hmcts-judiciary") && office.equals("EMPLOYMENT Tribunal Member Lay-Fee-Paid"))
            || r.getRoleName().equals("fee-paid-judge")
            || r.getRoleName().equals("tribunal-member")) {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
        } else {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
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
            "EMPLOYMENT President of Tribunal-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                    + "hmcts-judiciary,specific-access-approver-judiciary",
            "EMPLOYMENT Vice President-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                    + "hmcts-judiciary,specific-access-approver-judiciary",
            "EMPLOYMENT Regional Employment Judge-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                    + "hmcts-judiciary,specific-access-approver-judiciary"
    })
    void shouldReturnPresidentOfTribunalVicePresidentRegionalEmploymentJudgeSalariedRolesRoles(String setOffice,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(6, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYMENT Employment Judge-Salaried,judge,hmcts-judiciary"
    })
    void shouldReturnEmploymentJudgeSalariedRoles(String setOffice,
                                                  @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(2, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYMENT Employment Judge-Fee-Paid,fee-paid-judge,hmcts-judiciary"
    })
    void shouldReturnEmploymentJudgeFeePaidRoles(String setOffice,
                                                  @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(2, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYMENT Tribunal Member-Fee-Paid,tribunal-member",
            "EMPLOYMENT Tribunal Member Lay-Fee-Paid,tribunal-member"
    })
    void shouldReturnTribunalMemberFeePaidRoles(String setOffice,
                                                 @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(1, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }


}

