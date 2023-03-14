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

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolEmploymentJudicialRoleMappingTest extends DroolBase {

    void assertCommonRoleAssignmentAttributes(RoleAssignment r, String regionId) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(TestDataBuilder.id_2, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

        if (!r.getRoleName().contains("hmcts")) {
            assertEquals(regionId, r.getAttributes().get("region").asText());
            assertEquals(Classification.PUBLIC, r.getClassification());
            assertEquals(GrantType.STANDARD, r.getGrantType());
            assertEquals("EMPLOYMENT", r.getAttributes().get("jurisdiction").asText());
        } else {
            assertEquals(null, r.getAttributes().get("region"));
            assertEquals(Classification.PRIVATE, r.getClassification());
            assertEquals(GrantType.BASIC, r.getGrantType());
        }
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
            assertCommonRoleAssignmentAttributes(r, regionId);
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
            assertCommonRoleAssignmentAttributes(r, regionId);
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
        roleAssignments.forEach(r -> {
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            } else {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }
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
            assertCommonRoleAssignmentAttributes(r, regionId);
        });
    }


}

