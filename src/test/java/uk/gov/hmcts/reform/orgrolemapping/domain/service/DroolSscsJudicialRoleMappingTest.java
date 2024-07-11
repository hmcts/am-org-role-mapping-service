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
import uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertHelper.MultiRegion;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
class DroolSscsJudicialRoleMappingTest extends DroolBase {

    // NB: multi-regions are: South-West and Wales
    static  List<String> multiRegionList = List.of("6", "7");

    static Map<String, String> expectedRoleNameWorkTypesMap = new HashMap<>();

    static {
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

    static void assertCommonRoleAssignmentAttributes(RoleAssignment r,
                                                     Map<String, List<String>> roleNameToRegionsMap) {
        assertEquals(ActorIdType.IDAM, r.getActorIdType());
        assertEquals(RoleType.ORGANISATION, r.getRoleType());
        assertEquals(RoleCategory.JUDICIAL, r.getRoleCategory());

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
        // check region status and add to map
        MultiRegion.assertRegionStatusAndUpdateRoleToRegionMap(r, roleNameToRegionsMap);

        String expectedWorkTypes = expectedRoleNameWorkTypesMap.get(r.getRoleName());
        String actualWorkTypes = null;
        if (r.getAttributes().get("workTypes") != null) {
            actualWorkTypes = r.getAttributes().get("workTypes").asText();
        }
        assertEquals(expectedWorkTypes, actualWorkTypes);
    }

    static void assertRoleAssignmentAttributeBookable(RoleAssignment r, String ticketCode) {

        if (r.getRoleName().equals("fee-paid-judge")) {
            if (ticketCode != null && ticketCode.equals("368")) {
                assertTrue(r.getAttributes().get("bookable").asText().contains("true"));
            } else {
                assertNull(r.getAttributes().get("bookable"));
            }
        } else {
            assertNull(r.getAttributes().get("bookable"));
        }
    }


    @ParameterizedTest
    @CsvSource({
        "SSCS President of Tribunal-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',1,false",
        "SSCS President of Tribunal-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',6,false", // NB: no regions for PoT
        "SSCS President of Tribunal-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',7,false", // NB: no regions for PoT

        "SSCS Regional Tribunal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',1,false",
        "SSCS Regional Tribunal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',6,true",
        "SSCS Regional Tribunal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
            + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',7,true",

        "SSCS Principal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',1,false",
        "SSCS Principal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',6,true",
        "SSCS Principal Judge-Salaried,'leadership-judge,judge,post-hearing-salaried-judge,case-allocator,"
                + "task-supervisor,specific-access-approver-judiciary,hmcts-judiciary',7,true",

        "SSCS Judge of the First-tier Tribunal-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',1,false",
        "SSCS Judge of the First-tier Tribunal-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',6,true",
        "SSCS Judge of the First-tier Tribunal-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',7,true",

        "SSCS Tribunal Judge-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',1,false",
        "SSCS Tribunal Judge-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',6,true",
        "SSCS Tribunal Judge-Salaried,'judge,post-hearing-salaried-judge,hmcts-judiciary',7,true",

        "SSCS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary',1,false",
        "SSCS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary',6,true",
        "SSCS Tribunal Member Medical-Salaried,'medical,hmcts-judiciary',7,true",

        "SSCS Chief Medical Member First-tier Tribunal-Salaried,'medical,hmcts-judiciary',1,false",
        "SSCS Chief Medical Member First-tier Tribunal-Salaried,'medical,hmcts-judiciary',6,true",
        "SSCS Chief Medical Member First-tier Tribunal-Salaried,'medical,hmcts-judiciary',7,true",

        "SSCS Regional Medical Member-Salaried,'medical,hmcts-judiciary',1,false",
        "SSCS Regional Medical Member-Salaried,'medical,hmcts-judiciary',6,true",
        "SSCS Regional Medical Member-Salaried,'medical,hmcts-judiciary',7,true",
    })
    void shouldReturnSalariedRoles(String setOffice, String expectedRoles,
                                   String regionId, boolean expectMultiRegion) {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId(regionId);
        });

        // create map for all salaried roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "leadership-judge",
                "judge",
                "post-hearing-salaried-judge",
                "case-allocator",
                "task-supervisor",
                "specific-access-approver-judiciary",
                "medical"
        );
        if (setOffice.contains("President of Tribunal")) {
            rolesThatRequireRegions = List.of(); // NB: no regions for "President of Tribunal"
        }
        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());
        roleAssignments.forEach(r -> {
            assertEquals("Salaried", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, roleNameToRegionsMap);
            assertRoleAssignmentAttributeBookable(r,null);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                regionId, // fallback if not multi-region scenario
                null // i.e. no bookings
        );
    }

    @ParameterizedTest
    @CsvSource({
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        // NB: only testing multi-region when using fallback region
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created && ticketCode == 368
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Tribunal Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
            + "'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        // NB: only testing multi-region when using fallback region
        "SSCS Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
            + "'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
            + "'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created
        "SSCS Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
            + "'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Judge of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
            + "'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Chairman-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        "SSCS Chairman-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Chairman-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created
        "SSCS Chairman-Fee Paid,'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Chairman-Fee Paid,'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Deputy District Judge (MC)- Fee paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        "SSCS Deputy District Judge (MC)- Fee paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Deputy District Judge (MC)- Fee paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created
        "SSCS Deputy District Judge (MC)- Fee paid,'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Deputy District Judge (MC)- Fee paid,'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Employment Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        "SSCS Employment Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Employment Judge-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created
        "SSCS Employment Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Employment Judge-Fee Paid,'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Recorder-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,1,false,true",
        "SSCS Recorder-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,6,true,true",
        "SSCS Recorder-Fee Paid,'fee-paid-judge,judge,hmcts-judiciary',true,true,7,true,true",
        // ^ judge RA will be created if a booking created
        "SSCS Recorder-Fee Paid,'fee-paid-judge,hmcts-judiciary',true,false,1,false,false",
        // ^ judge RA will not be created, despite having a booking, JOH doesn't have ticketCode == 368
        "SSCS Recorder-Fee Paid,'fee-paid-judge,hmcts-judiciary',false,false,1,false,false",
        // ^ judge RA will not be created as there is no booking

        "SSCS Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Medical-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,7,true,false",

        "SSCS Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Optometrist-Fee Paid,'fee-paid-medical,hmcts-judiciary',false,false,7,true,false",

        "SSCS Tribunal Member Disability-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Disability-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Disability-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,7,true,false",

        "SSCS Member of the First-tier Tribunal Lay-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,1,false"
                + ",false",
        "SSCS Member of the First-tier Tribunal Lay-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,6,true"
                + ",false",
        "SSCS Member of the First-tier Tribunal Lay-Fee Paid,'fee-paid-disability,hmcts-judiciary',false,false,7,true"
                + ",false",

        "SSCS Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,7,true,false",

        "SSCS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Lay-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,7,true,false",

        "SSCS Tribunal Member Service-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Service-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Service-Fee Paid,'fee-paid-tribunal-member,hmcts-judiciary',false,false,7,true,false",

        "SSCS Member of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
                + "'fee-paid-disability,hmcts-judiciary',false,false,1,false,false",
        "SSCS Member of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
                + "'fee-paid-disability,hmcts-judiciary',false,false,6,true,false",
        "SSCS Member of the First-tier Tribunal (sitting in retirement)-Fee Paid,"
                + "'fee-paid-disability,hmcts-judiciary',false,false,7,true,false",

        "SSCS Tribunal Member Financially Qualified,'fee-paid-financial,hmcts-judiciary',false,false,1,false,false",
        "SSCS Tribunal Member Financially Qualified,'fee-paid-financial,hmcts-judiciary',false,false,6,true,false",
        "SSCS Tribunal Member Financially Qualified,'fee-paid-financial,hmcts-judiciary',false,false,7,true,false",

        "SSCS Member of the First-tier Tribunal-Fee Paid,'fee-paid-financial,hmcts-judiciary',false,false,1,false"
                + ",false",
        "SSCS Member of the First-tier Tribunal-Fee Paid,'fee-paid-financial,hmcts-judiciary',false,false,6,true,false",
        "SSCS Member of the First-tier Tribunal-Fee Paid,'fee-paid-financial,hmcts-judiciary',false,false,7,true,false",
    })
    void shouldReturnFeePaidRoles(String setOffice, String expectedRoles,
                                  boolean withBooking, boolean johFallback,
                                  String regionId, boolean expectMultiRegion,
                                  boolean with368Ticket) throws IOException {

        judicialOfficeHolders.forEach(joh -> {
            joh.setOffice(setOffice);
            joh.setRegionId(regionId);
            if (with368Ticket) {
                joh.setTicketCodes(List.of("368"));
            }
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

        // create map for all fee-paid roleNames that need regions
        List<String> rolesThatRequireRegions = List.of(
                "fee-paid-judge",
                "judge",
                "fee-paid-medical",
                "fee-paid-disability",
                "fee-paid-tribunal-member",
                "fee-paid-financial",
                "medical"
        );
        Map<String, List<String>> roleNameToRegionsMap = MultiRegion.buildRoleNameToRegionsMap(rolesThatRequireRegions);

        //Execute Kie session
        List<RoleAssignment> roleAssignments = buildExecuteKieSession(setFeatureFlags());

        //assertion
        List<String> expectedRoleList = Arrays.stream(expectedRoles.split(",")).toList();
        MultiRegion.assertRoleAssignmentCount(
                roleAssignments,
                expectedRoleList,
                expectMultiRegion,
                rolesThatRequireRegions,
                multiRegionList
        );

        assertEquals(judicialOfficeHolders.stream().iterator().next().getUserId(),roleAssignments.get(0).getActorId());

        roleAssignments.forEach(r -> {
            assertEquals("Fee-Paid", r.getAttributes().get("contractType").asText());
            assertCommonRoleAssignmentAttributes(r, roleNameToRegionsMap);
            String ticket = judicialOfficeHolders.stream().iterator().next().getTicketCodes() == null ? null :
                    judicialOfficeHolders.stream().iterator().next().getTicketCodes().iterator().next();
            assertRoleAssignmentAttributeBookable(r, ticket);
        });

        // verify regions add to map
        MultiRegion.assertRoleNameToRegionsMapIsAsExpected(
                roleNameToRegionsMap,
                expectedRoleList,
                expectMultiRegion,
                multiRegionList,
                regionId, // fallback if not multi-region scenario
                setExpectedBookingRegionId(regionId, withBooking, johFallback)
        );
    }


    private static List<FeatureFlag> setFeatureFlags() {
        List<String> flags = List.of("sscs_wa_1_0", "sscs_wa_1_1", "sscs_wa_1_2", "sscs_wa_1_3");

        return flags.stream()
                .map(flag -> FeatureFlag.builder().flagName(flag).status(true).build())
                .collect(Collectors.toList());
    }

    private String setExpectedBookingRegionId(String regionId, boolean withBooking, boolean johFallback) {
        if (withBooking && !judicialBookings.isEmpty()) {
            return (johFallback ? regionId :
                    judicialBookings.iterator().next().getRegionId());
        } else {
            return regionId;
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
