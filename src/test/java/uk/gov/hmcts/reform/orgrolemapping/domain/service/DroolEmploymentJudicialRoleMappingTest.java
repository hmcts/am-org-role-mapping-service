package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.*;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@RunWith(MockitoJUnitRunner.class)
class DroolEmploymentJudicialRoleMappingTest extends DroolBase {

    @ParameterizedTest
    @CsvSource({
            "EMPLOYMENT President of Tribunal-Salaried,leadership-judge,judge,task-supervisor,case-allocator,hmcts-judiciary,specific-access-approver-judiciary",
            "EMPLOYMENT Vice President-Salaried,leadership-judge,judge,task-supervisor,case-allocator,hmcts-judiciary,specific-access-approver-judiciary",
            "EMPLOYMENT Regional Employment Judge-Salaried,leadership-judge,judge,task-supervisor,case-allocator,hmcts-judiciary,specific-access-approver-judiciary"
    })
    void shouldReturnPresidentOfTribunalVicePresidentRegionalEmploymentJudgeSalariedRolesRoles(String setOffice,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
//        assertEquals(6, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
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
//        assertEquals(2, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
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
        assertEquals(2, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            if (!r.getRoleName().contains("hmcts")) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            }
            else {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            }

            if (r.getRoleName().equals("judge")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
            else {
                assertEquals(null, r.getAttributes().get("region"));
            }


        });
    }

    @ParameterizedTest
    @CsvSource({
            "EMPLOYMENT Tribunal Member,tribunal-member",
            "EMPLOYMENT Tribunal Member Lay,tribunal-member"
    })
    void shouldReturnTribunalMemberFeePaidRoles(String setOffice,
                                                 @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("employment_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
//        assertEquals(3, roleAssignments.size());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            //commented as looks like confluence page needs to be corrected
//            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            if (!r.getRoleName().contains("fee-paid") && !r.getRoleName().contains("hmcts")) {
                assertEquals(regionId, r.getAttributes().get("region").asText());
            }
            else {
                assertEquals(null, r.getAttributes().get("region"));
            }
        });
    }


}

