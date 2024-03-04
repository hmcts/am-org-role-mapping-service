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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsJudicialRoleMappingTest extends DroolBase {

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    {
        expectedRoleNameWorkTypesMap.put("leadership-judge", null);
        expectedRoleNameWorkTypesMap.put("judge", "pre_hearing,hearing_work,post_hearing,"
                + "decision_making_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("post-hearing-salaried-judge", null);
        expectedRoleNameWorkTypesMap.put("case-allocator", null);
        expectedRoleNameWorkTypesMap.put("task-supervisor", null);
        expectedRoleNameWorkTypesMap.put("hmcts-judiciary", null);
        expectedRoleNameWorkTypesMap.put("specific-access-approver-judiciary", "access_requests");
        expectedRoleNameWorkTypesMap.put("fee-paid-judge", "pre_hearing,hearing_work,post_hearing,"
                + "decision_making_work,routine_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-tribunal-member", "hearing_work,priority");
        expectedRoleNameWorkTypesMap.put("medical", "hearing_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-medical", "hearing_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-disability", "hearing_work,priority");
        expectedRoleNameWorkTypesMap.put("fee-paid-financial", "hearing_work,priority");
    }

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r, String regionId, String office) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());
        if (!r.getRoleName().equals("fee-paid-judge")) {
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
            assertEquals("SSCS", r.getAttributes().get("jurisdiction").asText());
            assertFalse(r.isReadOnly());
            assertEquals("2", primaryLocation);
        }
        //region assertions
        if (r.getRoleName().equals("hmcts-judiciary")
                || office.contains("President of Tribunal")) {
            assertNull(r.getAttributes().get("region"));
        } else {
            assertEquals(regionId, r.getAttributes().get("region").asText());
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
        "SSCS President of Tribunal-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
        "SSCS Regional Tribunal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary'",
        "SSCS Tribunal Judge-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary'",
        "SSCS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary'",
    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles) {

        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());
        String regionId = allProfiles.iterator().next().getRegionId();
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }

    @ParameterizedTest
    @CsvSource({
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true, true",
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true, false",
        // ^ judge RA will be created if a booking created
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary',false, false",
        // ^ judge RA will not be created as there is no booking
        "SSCS Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary',false, false",
        "SSCS Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary',false, false",
        "SSCS Tribunal Member Disability-Fee Paid,'fee-paid-disability,hmcts-judiciary',false, false",
        "SSCS Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false, false",
        "SSCS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false, false",
        "SSCS Tribunal Member Service-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false, false",
        "SSCS Tribunal Member Financially Qualified,'fee-paid-financial,hmcts-judiciary',false, false"
    })
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles, boolean withBooking,
                                  boolean johFallback) throws IOException {
        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setTicketCodes(List.of("368"));
        });

        if (withBooking) {
            createBooking(setOffice);
            if (johFallback) {
                judicialBookings.forEach(jb -> {
                    jb.setRegionId(null);
                    jb.setLocationId(null);
                });
            }
        }

        //Execute Kie session
        List<RoleAssignment> roleAssignments =
                buildExecuteKieSession(getFeatureFlags("sscs_wa_1_0", true));

        //assertion
        assertFalse(roleAssignments.isEmpty());
        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        assertThat(roleAssignments.stream().map(RoleAssignment::getRoleName).collect(Collectors.toList()),
                containsInAnyOrder(expectedRoles.split(",")));
        assertEquals(expectedRoles.split(",").length, roleAssignments.size());

        roleAssignments.forEach(r -> {
            String regionId = setExpectedRegionId(setOffice,withBooking,johFallback,r);
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, regionId, setOffice);
        });
    }

    private String setExpectedRegionId(String setOffice, boolean withBooking,
                                       boolean johFallback, RoleAssignment roleAssignment) {
        if (setOffice.equals("SSCS Tribunal Judge-Fee Paid") && withBooking
                && roleAssignment.getRoleName().equals("judge")) {
            if (!judicialBookings.isEmpty()) {
                return (johFallback ? judicialOfficeHolders.iterator().next().getRegionId() :
                        judicialBookings.iterator().next().getRegionId());
            } else {
                return allProfiles.iterator().next().getRegionId();
            }
        } else {
            return allProfiles.iterator().next().getRegionId();
        }
    }


    void createBooking(String setOffice) throws IOException {
        judicialOfficeHolders.forEach(joh -> joh.setOffice(setOffice));
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        judicialBooking.setUserId(judicialOfficeHolders.stream().findFirst()
                .orElse(JudicialOfficeHolder.builder().build()).getUserId());
        judicialBooking.setLocationId("2");
        judicialBookings = Set.of(judicialBooking);
    }
}
