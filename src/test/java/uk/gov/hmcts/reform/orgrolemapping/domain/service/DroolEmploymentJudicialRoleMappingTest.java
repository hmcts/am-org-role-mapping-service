package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.VarargsAggregator;

@ExtendWith(MockitoExtension.class)
class DroolEmploymentJudicialRoleMappingTest extends DroolBase {
    static Map<String, String> employmentExpectedRoleNameWorkTypesMap = new HashMap<>();

    static {
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
        assertEquals(null, r.getAttributes().get("bookable"));

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
        "EMPLOYMENT President Employment Tribunals (Scotland)-Salaried,leadership-judge,judge,task-supervisor,"
                    + "case-allocator,hmcts-judiciary,specific-access-approver-judiciary",
        "EMPLOYMENT Vice-President Employment Tribunal (Scotland)-Salaried,leadership-judge,judge,task-supervisor,"
                + "case-allocator,hmcts-judiciary,specific-access-approver-judiciary",
        "EMPLOYMENT Vice President-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                + "hmcts-judiciary,specific-access-approver-judiciary",
        "EMPLOYMENT Regional Employment Judge-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                + "hmcts-judiciary,specific-access-approver-judiciary",
        "EMPLOYMENT Acting Regional Employment Judge-Salaried,leadership-judge,judge,task-supervisor,case-allocator,"
                + "hmcts-judiciary,specific-access-approver-judiciary",
    })
    void shouldReturnPresidentOfTribunalVicePresidentRegionalEmploymentJudgeSalariedRoles(String office,
                                         @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(office);
            // NB: joh.contractType populated with jap.appointmentType
            joh.setContractType(AppointmentType.SALARIED);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(6, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, office);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "EMPLOYMENT Employment Judge-Salaried,judge,hmcts-judiciary,case-allocator"
    })
    void shouldReturnEmploymentJudgeSalariedRoles(String office,
                                                  @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(office);
            // NB: joh.contractType populated with jap.appointmentType
            joh.setContractType(AppointmentType.SALARIED);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());
        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(Arrays.stream(roleNameOutput).count(), roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, office);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "EMPLOYMENT Employment Judge-Fee-Paid,fee-paid-judge,hmcts-judiciary"
    })
    void shouldReturnEmploymentJudgeFeePaidRoles(String office,
                                                  @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(office);
            // NB: joh.contractType populated with jap.appointmentType
            joh.setContractType(AppointmentType.FEE_PAID);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(2, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertCommonRoleAssignmentAttributes(r, regionId, office);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "EMPLOYMENT Tribunal Member-Fee-Paid,tribunal-member,hmcts-judiciary",
        "EMPLOYMENT Tribunal Member Lay-Fee-Paid,tribunal-member,hmcts-judiciary"
    })
    void shouldReturnTribunalMemberFeePaidRoles(String office,
                                                 @AggregateWith(VarargsAggregator.class) String[] roleNameOutput) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(office);
            // NB: joh.contractType populated with jap.appointmentType
            joh.setContractType(AppointmentType.FEE_PAID);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(roleNameOutput));
        assertEquals(2, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertCommonRoleAssignmentAttributes(r, regionId, office);
        });
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';',  textBlock = """ 
        President of Tribunal;65;Salaried;12;11;
        President, Employment Tribunals (Scotland);153;Salaried;12;11;
        President, Employment Tribunals (Scotland);153;Salaried;11;11;
        Vice President;91;Salaried;13;13;
        Vice-President, Employment Tribunal (Scotland);213;Salaried;12;11;
        Regional Employment Judge;71;Salaried;10;10;
        Employment Judge;48;Salaried;11;11;
        Employment Judge;48;Fee Paid;12;11;
        Tribunal Member;85;Fee Paid;11;11;
        """)
    void shouldReturnRegionIdFromJapAsRegion(String appointment, String appointmentCode, String appointmentType,
                                             String regionIn, String regionOut) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment(appointment);
            judicialAccessProfile.setAppointmentCode(appointmentCode);
            judicialAccessProfile.setAppointmentType(appointmentType);
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCodes(List.of("BHA1")));
            judicialAccessProfile.setRegionId(regionIn);
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        roleAssignments.forEach(r -> {
            if (r.getAttributes().get("region") != null) {
                assertEquals(regionOut, r.getAttributes().get("region").asText());
            }
        });
    }

    private List<FeatureFlag> setFeatureFlags() {
        return getAllFeatureFlagsToggleByJurisdiction("EMPLOYMENT", true);
    }

}

