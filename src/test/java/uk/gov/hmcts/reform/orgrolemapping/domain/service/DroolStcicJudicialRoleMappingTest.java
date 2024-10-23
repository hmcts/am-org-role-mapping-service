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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolStcicJudicialRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
        expectedRoleNameWorkTypesMap.put("senior-judge", null);
        expectedRoleNameWorkTypesMap.put("judge", "decision_making_work");
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "decision_making_work");
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String office, List<String> ticketCodes) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(TestDataBuilder.id_2, r.getActorId());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
        if (r.getRoleName().equals("fee-paid-judge") && ticketCodes != null && ticketCodes.contains("328")) {
            assertTrue(r.getAttributes().get("bookable").asBoolean());
        } else {
            assertNull(r.getAttributes().get("bookable"));
        }

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
        
        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    @ParameterizedTest
    @CsvSource({
        "ST_CIC President of Tribunal-Salaried,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                + "hmcts-judiciary,specific-access-approver-judiciary'",
        "ST_CIC Principal Judge-Salaried,'leadership-judge,senior-judge,judge,case-allocator,task-supervisor,"
                + "hmcts-judiciary,specific-access-approver-judiciary'",
        "ST_CIC Tribunal Judge-Salaried,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "ST_CIC Judge of the First-tier Tribunal-Salaried,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "ST_CIC Circuit Judge-Salaried,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
                + "specific-access-approver-judiciary'",
        "ST_CIC Regional Tribunal Judge-Salaried,'judge,hmcts-judiciary,case-allocator,task-supervisor,"
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
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, setOffice, null);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "ST_CIC Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Chairman-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Recorder-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Deputy Upper Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary'",
        "ST_CIC Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary'",
        "ST_CIC Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary'",
        "ST_CIC Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary'",
        "ST_CIC Tribunal Member Disability-Fee Paid,'fee-paid-disability,fee-paid-tribunal-member,hmcts-judiciary'",
        "ST_CIC Member of the First-tier Tribunal (sitting in retirement)-Fee Paid,'fee-paid-disability,"
                + "fee-paid-tribunal-member,hmcts-judiciary'",
        "ST_CIC Tribunal Member Financially Qualified-Fee Paid,'fee-paid-financial,hmcts-judiciary'"
    })
    void verifyFeePaidRoles(String setOffice, String expectedRoles) throws IOException {
        shouldReturnFeePaidRoles(setOffice, expectedRoles, false, "");
    }

    @ParameterizedTest
    @CsvSource({
        "ST_CIC Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'",
        "ST_CIC Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,'fee-paid-judge,"
                + "hmcts-judiciary,judge'",
        "ST_CIC Chairman-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'",
        "ST_CIC Recorder-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'",
        "ST_CIC Deputy Upper Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary,judge'"
    })
    void verifyFeePaidRolesWithBookingAndValidTicketCode(String setOffice, String expectedRoles) throws IOException {
        shouldReturnFeePaidRoles(setOffice, expectedRoles, true, "328");
    }

    @ParameterizedTest
    @CsvSource({
        "ST_CIC Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Chairman-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Recorder-Fee Paid,'fee-paid-judge,hmcts-judiciary'",
        "ST_CIC Deputy Upper Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary'"
    })
    void verifyFeePaidRolesWithBookingAndInvalidTicketCode(String setOffice, String expectedRoles) throws IOException {
        shouldReturnFeePaidRoles(setOffice, expectedRoles, true, "");
    }

    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles,
                                  boolean withBooking, String ticketCode) throws IOException {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setTicketCodes(List.of(ticketCode));
        });

        if (withBooking) {
            JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
            judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                    .orElse(JudicialOfficeHolder.builder().build()).getUserId());
            judicialBooking.setLocationId("2");
            judicialBookings = Set.of(judicialBooking);
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        List<String> ticketCodes = judicialOfficeHolders.stream()
                .flatMap(judicialOfficeHolder -> judicialOfficeHolder.getTicketCodes().stream())
                .toList();
        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, setOffice, ticketCodes);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "ST_CIC Advisory Committee Member - Magistrate-Voluntary,'magistrate'",
        "ST_CIC Magistrate-Voluntary,'magistrate'"
    })
    void shouldReturnVoluntaryRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setTicketCodes(List.of(""));
        });

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("st_cic_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).toList(),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        roleAssignments.forEach(r -> {
            assertEquals("Voluntary", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, setOffice, null);
        });
    }

}
