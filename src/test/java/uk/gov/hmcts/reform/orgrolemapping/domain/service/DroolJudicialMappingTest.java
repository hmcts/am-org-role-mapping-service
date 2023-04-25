package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialMappingTest extends DroolBase {

    String workTypes = "hearing_work,upper_tribunal,decision_making_work,applications";
    String workTypesFP = "hearing_work,decision_making_work,applications";
    String workTypesAccess = "hearing_work,upper_tribunal,decision_making_work,applications,access_requests";

    @Test
    void shouldReturnPresidentRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC President of Tribunals"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "senior-judge",
                        "case-allocator", "judge", "hmcts-legal-operations",
                        "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName()) || "senior-judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnResidentJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Resident Immigration Judge"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(9, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "senior-judge",
                        "case-allocator", "task-supervisor", "leadership-judge", "judge",
                        "hmcts-legal-operations", "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName()) || "senior-judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            } else if ("leadership-judge".equals(r.getRoleName())) {
                assertEquals(workTypesAccess, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnImmigrationJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Designated Immigration Judge"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "leadership-judge",
                        "case-allocator", "task-supervisor", "judge", "hmcts-legal-operations",
                        "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            } else if ("leadership-judge".equals(r.getRoleName())) {
                assertEquals(workTypesAccess, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnAssistantResidentJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Assistant Resident Judge"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(8, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "leadership-judge",
                        "case-allocator", "task-supervisor", "judge", "hmcts-legal-operations",
                        "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            } else if ("leadership-judge".equals(r.getRoleName())) {
                assertEquals(workTypesAccess, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnSalariedTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Tribunal Judge (Salaried)"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "case-allocator", "judge",
                        "hmcts-legal-operations", "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Salaried", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles() {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Tribunal Judge (Fee-Paid)"));

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(5, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "fee-paid-judge",
                        "hmcts-legal-operations", "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("fee-paid-judge".equals(r.getRoleName())) {
                assertEquals(workTypesFP, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles_withBookings() throws IOException {

        judicialOfficeHolders.forEach(joh -> joh.setOffice("IAC Tribunal Judge (Fee-Paid)"));
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        judicialBookings = Set.of(judicialBooking);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(6, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "fee-paid-judge", "judge",
                        "hmcts-legal-operations", "tribunal-caseworker", "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("judge".equals(r.getRoleName())) {
                assertEquals(judicialBooking.getUserId(), r.getActorId());
                assertEquals(judicialBooking.getLocationId(), r.getAttributes().get("primaryLocation").asText());
                assertEquals(judicialBooking.getBeginTime(), r.getBeginTime());
                assertEquals(judicialBooking.getEndTime(), r.getEndTime());
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
            } else if ("fee-paid-judge".equals(r.getRoleName())) {
                assertEquals(workTypesFP, r.getAttributes().get("workTypes").asText());
            }
        });
    }

    @Test
    void shouldReturnFeePaidTribunalJudgeRoles_withMultipleBookings() throws IOException {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice("IAC Tribunal Judge (Fee-Paid)");
            joh.setPrimaryLocation("Judicial Location");
        });
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("location1");
        JudicialBooking judicialBooking2 = TestDataBuilder.buildJudicialBooking();
        judicialBooking2.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking2.setLocationId(null);
        judicialBooking2.setBeginTime(ZonedDateTime.now().minusDays(5));
        judicialBookings = Set.of(judicialBooking, judicialBooking2);
        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(emptyList());

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(7, roleAssignments.size());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                IsIterableContainingInAnyOrder.containsInAnyOrder("hmcts-judiciary", "fee-paid-judge",
                        "judge", "judge", "hmcts-legal-operations", "tribunal-caseworker",
                        "senior-tribunal-caseworker"));

        roleAssignments.forEach(r -> {
            if ("judge".equals(r.getRoleName())) {
                assertEquals(workTypes, r.getAttributes().get("workTypes").asText());
                assertEquals(judicialBooking2.getUserId(), r.getActorId());
            } else if (!"hmcts-legal-operations".equals(r.getRoleName())
                    && !"tribunal-caseworker".equals(r.getRoleName())
                    && !"senior-tribunal-caseworker".equals(r.getRoleName())
            ) {
                assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
                assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(), r.getActorId());
            }

            if ("fee-paid-judge".equals(r.getRoleName())) {
                assertEquals(workTypesFP, r.getAttributes().get("workTypes").asText());
            }
        });

        List<RoleAssignment> judgeRoleAssignments = roleAssignments.stream()
                .filter(r -> "judge".equals(r.getRoleName()))
                .toList();
        RoleAssignment assignment = judgeRoleAssignments.get(0);
        RoleAssignment assignment2 = judgeRoleAssignments.get(1);

        assertThat(List.of(judicialBooking.getLocationId(),
                        judicialOfficeHolders.stream().findFirst().get().getPrimaryLocation()),
                containsInAnyOrder(assignment.getAttributes().get("primaryLocation").asText(),
                        assignment2.getAttributes().get("primaryLocation").asText()));
        assertThat(List.of(judicialBooking.getBeginTime(), judicialBooking2.getBeginTime()),
                containsInAnyOrder(assignment.getBeginTime(), assignment2.getBeginTime()));
        assertThat(List.of(judicialBooking.getEndTime(), judicialBooking2.getEndTime()),
                containsInAnyOrder(assignment.getEndTime(), assignment2.getEndTime()));

    }
}
