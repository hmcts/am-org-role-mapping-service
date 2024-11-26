package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.RAS_CREATE_ASSIGNMENTS_URL;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.AM_ORG_ROLE_MAPPING_SERVICE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.IDAM_ID;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;

@Slf4j
public class RoleAssignmentAssertIntegrationHelper {

    public static void assertWireMockAssignmentRequests(List<AssignmentRequest> expectedAssignmentRequests,
                                                        List<TestScenario> testScenarios)
        throws IOException {

        Map<String, AssignmentRequest> requestMap = getMapOfRasRequests();

        assertEquals(expectedAssignmentRequests.size(), requestMap.size(), "Count of scenarios");

        for (AssignmentRequest expectedAssignmentRequest: expectedAssignmentRequests) {
            AssignmentRequest actualAssignmentRequest
                = requestMap.get(getAssignmentRequestKey(expectedAssignmentRequest));

            TestScenario testScenario = findTestScenarioFromIdamId(
                expectedAssignmentRequest.getRequest().getReference(),
                testScenarios
            );
            log.info("#####################################################");
            log.info("ASSERT for: {}", testScenario.getDescription());
            log.info("... with overrides for: {}", testScenario.getReplaceMap());
            log.info("#####################################################");
            log.info("Expected AssignmentRequest: {}", writeValueAsPrettyJson(expectedAssignmentRequest));
            log.info("Actual AssignmentRequest: {}", writeValueAsPrettyJson(actualAssignmentRequest));

            assertNotNull(actualAssignmentRequest, "AssignmentRequest");
            assertRoleRequest(expectedAssignmentRequest, actualAssignmentRequest);
            assertRequestedRoles(expectedAssignmentRequest, actualAssignmentRequest);
        }
    }

    private static TestScenario findTestScenarioFromIdamId(String idamId, List<TestScenario> testScenarios) {
        return testScenarios.stream()
            .filter(testScenario -> testScenario.getReplaceMap().get(IDAM_ID).equals(idamId))
            .findFirst()
            .orElse(TestScenario.builder().description("No description found for idamId: " + idamId).build());
    }

    private static String writeValueAsPrettyJson(Object input) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
    }

    private static void assertRoleRequest(AssignmentRequest expectedAssignmentRequest,
                                          AssignmentRequest assignmentRequest) {

        var expectedRoleRequest = expectedAssignmentRequest.getRequest();
        var roleRequest = assignmentRequest.getRequest();

        assertAll(
            () -> assertEquals(AM_ORG_ROLE_MAPPING_SERVICE, roleRequest.getClientId(), "Request.ClientId"),
            () -> assertEquals(RequestType.CREATE, roleRequest.getRequestType(), "Request.RequestType"),

            () -> assertEquals(expectedRoleRequest.getProcess(), roleRequest.getProcess(), "Request.Process"),
            () -> assertEquals(expectedRoleRequest.getReference(), roleRequest.getReference(), "Request.Reference"),

            () -> assertTrue(roleRequest.isReplaceExisting())
        );
    }

    private static void assertRequestedRoles(AssignmentRequest expectedAssignmentRequest,
                                             AssignmentRequest actualAssignmentRequest) {

        var expectedRequestedRoles = expectedAssignmentRequest.getRequestedRoles();
        var actualRequestedRoles = actualAssignmentRequest.getRequestedRoles();

        assertEquals(expectedRequestedRoles.size(), actualRequestedRoles.size(), "RequestedRoles.size");

        Map<String, RoleAssignment> roleAssignmentMap = createMapOfRoleAssignments(actualRequestedRoles);

        for (RoleAssignment expectedRoleAssignment: expectedRequestedRoles) {
            String assignmentKey = getRoleAssignmentKey(expectedRoleAssignment);
            log.info("ASSERT: validating assignmentKey: {}", assignmentKey);

            RoleAssignment actualRoleAssignment = roleAssignmentMap.get(assignmentKey);

            assertNotNull(actualRoleAssignment, "RoleAssignment.notNull");
            assertRoleAssignment(expectedRoleAssignment, actualRoleAssignment);
        }

    }

    private static void assertRoleAssignment(RoleAssignment expected,
                                             RoleAssignment actual) {

        assertAll(
            () -> assertEquals(expected.getActorIdType(), actual.getActorIdType(), "ActorIdType"),
            () -> assertEquals(expected.getActorId(), actual.getActorId(), "ActorIdType"),
            () -> assertEquals(expected.getRoleType(), actual.getRoleType(), "RoleType"),
            () -> assertEquals(expected.getRoleName(), actual.getRoleName(), "RoleName"),
            () -> assertEquals(expected.getClassification(), actual.getClassification(), "Classification"),
            () -> assertEquals(expected.getGrantType(), actual.getGrantType(), "GrantType"),
            () -> assertEquals(expected.getRoleCategory(), actual.getRoleCategory(), "RoleCategory"),
            () -> assertEquals(expected.isReadOnly(), actual.isReadOnly(), "ReadOnly"),
            () -> assertEquals(expected.getBeginTime(), actual.getBeginTime(), "BeginTime"),
            () -> assertEquals(expected.getEndTime(), actual.getEndTime(), "EndTime"),
            () -> assertRoleAssignmentAttributes(expected, actual),
            () -> assertRoleAssignmentAuthorisations(expected, actual)
        );
    }

    private static void assertRoleAssignmentAttributes(RoleAssignment expectedRoleAssignment,
                                                       RoleAssignment actualRoleAssignment) {

        var expectedAttributes = expectedRoleAssignment.getAttributes();
        var actualAttributes = actualRoleAssignment.getAttributes();

        if (expectedAttributes == null) {
            assertNull(actualAttributes, "Attributes.null");
        } else {
            assertNotNull(actualAttributes, "Attributes.notNull");
            assertEquals(expectedAttributes.size(), actualAttributes.size(), "Attributes.size");

            for (String key : expectedRoleAssignment.getAttributes().keySet()) {
                assertTrue(actualAttributes.containsKey(key), "Attributes.key");
                assertEquals(expectedAttributes.get(key), actualAttributes.get(key), "Attributes.value " + key);
            }
        }
    }

    private static void assertRoleAssignmentAuthorisations(RoleAssignment expectedRoleAssignment,
                                                           RoleAssignment actualRoleAssignment) {

        var expectedAuthorisations = expectedRoleAssignment.getAuthorisations();
        var actualAuthorisations = actualRoleAssignment.getAuthorisations();

        if (expectedAuthorisations == null) {
            assertNull(actualAuthorisations, "Authorisations.null");
        } else {
            assertNotNull(actualAuthorisations, "Authorisations.notNull");
            assertEquals(expectedAuthorisations.size(), actualAuthorisations.size(), "Authorisations.size");
            assertTrue(actualAuthorisations.containsAll(expectedAuthorisations), "Authorisations mismatch");
        }
    }

    private static Map<String, RoleAssignment> createMapOfRoleAssignments(Collection<RoleAssignment> roleAssignments) {
        Map<String, RoleAssignment> roleAssignmentMap  = new HashMap<>();

        for (RoleAssignment roleAssignment : roleAssignments) {
            roleAssignmentMap.put(getRoleAssignmentKey(roleAssignment), roleAssignment);
        }

        return roleAssignmentMap;
    }

    private static Map<String, AssignmentRequest> getMapOfRasRequests() throws IOException {
        Map<String, AssignmentRequest> requestMap = new HashMap<>();

        var allLoggedRequests = WIRE_MOCK_SERVER.findAll(postRequestedFor(urlEqualTo(RAS_CREATE_ASSIGNMENTS_URL)));

        for (LoggedRequest allLoggedRequest : allLoggedRequests) {
            AssignmentRequest assignmentRequest
                = MAPPER.readValue(allLoggedRequest.getBody(), AssignmentRequest.class);
            requestMap.put(getAssignmentRequestKey(assignmentRequest), assignmentRequest);
        }

        return requestMap;
    }

    private static String getAssignmentRequestKey(AssignmentRequest assignmentRequest) {
        return assignmentRequest.getRequest().getProcess() + "__" + assignmentRequest.getRequest().getReference();
    }

    private static String getRoleAssignmentKey(RoleAssignment roleAssignment) {
        return roleAssignment.getRoleType()
            + "__" + roleAssignment.getRoleCategory()
            + "__" + roleAssignment.getRoleName()
            + "__" + getAttributeValue(roleAssignment, "jurisdiction")
            + "__" + getAttributeValue(roleAssignment, "region");
    }

    private static String getAttributeValue(RoleAssignment roleAssignment, String attributeName) {
        return roleAssignment.getAttributes().containsKey(attributeName)
            ? roleAssignment.getAttributes().get(attributeName).asText()
            : "null";
    }

}
