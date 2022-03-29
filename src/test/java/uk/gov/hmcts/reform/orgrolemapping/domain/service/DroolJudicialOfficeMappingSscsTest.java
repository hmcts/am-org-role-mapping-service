package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;


@RunWith(MockitoJUnitRunner.class)
class DroolJudicialOfficeMappingSscsTest extends DroolBase {

    @Test
    void shouldReturnTribunalJudgeSalariedRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("President of Tribunal");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> a.setServiceCode("BBA3"));
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });

        assertEquals("caseworker-sscs-judge", roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor", roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(3).getRoleName());
        assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());


    }

    @ParameterizedTest
    @CsvSource({"caseworker-sscs-judge","case-allocator","task-supervisor","hmcts-judiciary",})
    void shouldReturnRegionalTribunalSalariedRoles(String roles) {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Regional Tribunal Judge");
        });

        JudicialAccessProfile profile = TestDataBuilder.buildJudicialAccessProfile();
        profile.setAppointmentType("Salaried");
        profile.setServiceCode("BBA3");
        profile.getAuthorisations().forEach(a -> a.setServiceCode("BBA3"));
        judicialAccessProfiles.add(profile);

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }

        });
        assertEquals("caseworker-sscs-judge", roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor", roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(3).getRoleName());
        assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_judge_fee_paid_joh
    @Test
    void shouldReturnTribunalFeePaidJudgeRoles_withSSCS() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("caseworker-sscs-judge-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,decision_making_work,routine_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_judge_salaried_joh
    @Test
    void shouldReturnTribunalJudgeSalariedRoleswithSCSS() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });
        assertEquals("caseworker-sscs-judge", roleAssignments.get(0).getRoleName());
        assertEquals("case-allocator", roleAssignments.get(1).getRoleName());
        assertEquals("task-supervisor", roleAssignments.get(2).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(3).getRoleName());

        assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());


    }

    //sscs_tribunal_member_medical_fee_paid_joh
    @Test
    void shouldReturnTribunalMemberMedicalFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal member medical");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("caseworker-sscs-medical-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_member_medical_salaried_joh
    @Test
    void shouldReturnTribunalMemberMedicalSalariedRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal member medical");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        assertEquals("case-allocator", roleAssignments.get(0).getRoleName());
        assertEquals("task-supervisor", roleAssignments.get(1).getRoleName());
        assertEquals("hmcts-judiciary", roleAssignments.get(2).getRoleName());
        assertEquals("caseworker-sscs-medical", roleAssignments.get(3).getRoleName());

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
        });

        assertEquals("hearing_work,decision_making_work,routine_work,priority",
                roleAssignments.get(3).getAttributes().get("workTypes").asText());



    }

    //sscs_tribunal_member_disability_fee_paid_joh
    @Test
    void shouldReturnTribunalMemberDisabilityFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal member disability");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-disability-feepaid", roleAssignments.get(0).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_member_financially_qualified_joh
    @Test
    void shouldReturnTribunalMemberFinanciallyQualifiedRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal member financially qualified");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");
            });
        });


        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        assertEquals("caseworker-sscs-financial-feepaid", roleAssignments.get(0).getRoleName());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());


    }

    //sscs_tribunal_member_fee_paid_joh
    @Test
    void shouldReturnTribunalMemberFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Member");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());


        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("caseworker-sscs-disability-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());



    }

    //sscs_tribunal_member_lay_fee_paid_joh
    @Test
    void shouldReturnTribunalMembeLayrFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Member Lay");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("caseworker-sscs-disability-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_member_optometrist_fee_paid_joh
    @Test
    void shouldReturnTribunalOptometristFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Member Optometrist");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());


        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });
        assertEquals("caseworker-sscs-medical-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //sscs_tribunal_member_service_fee_paid_joh
    @Test
    void shouldReturnTribunalMemberServiceFeePaidRoles() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Tribunal Member Service");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());

        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
        });

        assertEquals("caseworker-sscs-disability-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

    //=========================SPECIAL RULES HERE ================================================

    //sscs_district_tribunal_judge_salaried_joh
    @Test
    void shouldReturnTribunalDistrictJudgeSalariedSetUpRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("District Tribunal Judge"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("caseworker-sscs-judge", "case-allocator", "task-supervisor", "hmcts-judiciary",
                        "caseworker-sscs-judge", "case-allocator", "task-supervisor", "hmcts-judiciary"
                        ));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));
            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS",r.getAttributes().get("jurisdiction").asText());
                assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                        roleAssignments.get(0).getAttributes().get("workTypes").asText());
            }
        });

    }

    //sscs_regional_medical_member_salaried_joh
    @Test
    void shouldReturnRegionalMedicalSalariedSetUpRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setRoles(List.of("Regional Medical Member"));
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");

            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        System.out.println(roleAssignments);

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("caseworker-sscs-medical", "hmcts-judiciary", "case-allocator", "task-supervisor",
                        "caseworker-sscs-judge", "hmcts-judiciary", "case-allocator", "task-supervisor"
                ));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
                assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                        roleAssignments.get(0).getAttributes().get("workTypes").asText());
            }

        });

    }

    //sscs_tribunal_judge_salaried_default_joh
    @Test
    void shouldReturnTribunalJudgeSalariedDefaultSetUpRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Judge");
            judicialAccessProfile.setAppointmentType("Salaried");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");
            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(4, roleAssignments.size());
        System.out.println(roleAssignments);
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder("caseworker-sscs-judge", "case-allocator", "task-supervisor", "hmcts-judiciary"
                ));
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            if ("hmcts-judiciary".equals(r.getRoleName())) {
                assertNull(r.getAuthorisations());
                assertNull(r.getAttributes().get("primaryLocation"));

            } else {
                assertEquals("[373]", r.getAuthorisations().toString());
                assertEquals("primary location", r.getAttributes().get("primaryLocation").asText());
                assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());

            }
            assertEquals("hearing_work,decision_making_work,routine_work,access_requests,priority",
                    roleAssignments.get(0).getAttributes().get("workTypes").asText());

        });

    }

    //sscs_tribunal_judge_feepaid_joh
    @Test
    void shouldReturnTribunalJudgeFeePaidDefaultSetUpRole() {

        judicialAccessProfiles.forEach(judicialAccessProfile -> {
            judicialAccessProfile.setAppointment("Judge");
            judicialAccessProfile.setAppointmentType("Fee Paid");
            judicialAccessProfile.setServiceCode("BBA3");
            judicialAccessProfile.getAuthorisations().forEach(a -> {
                a.setServiceCode("BBA3");
            });
        });

        //Execute Kie session
        buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(1, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals(judicialAccessProfiles.stream().iterator().next().getUserId(), r.getActorId());
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
        });
        assertEquals("caseworker-sscs-judge-feepaid", roleAssignments.get(0).getRoleName());
        assertEquals("Fee-Paid", roleAssignments.get(0).getAttributes().get("contractType").asText());
        assertEquals("SSCS", roleAssignments.get(0).getAttributes().get("jurisdiction").asText());
        assertEquals("[373]", roleAssignments.get(0).getAuthorisations().toString());
        assertEquals("primary location",roleAssignments.get(0).getAttributes().get("primaryLocation").asText());
        assertEquals("hearing_work,decision_making_work,routine_work,priority",
                roleAssignments.get(0).getAttributes().get("workTypes").asText());

    }

}
