package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.RAS_CREATE_ASSIGNMENTS_URL;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;

@Slf4j
public class RoleAssignmentAssertIntegrationHelper {
 
    public static Map<String, AssignmentRequest> getMapOfRasRequests() throws IOException {
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

    private static String getAttributeValue(RoleAssignment roleAssignment, String attributeName) {
        return roleAssignment.getAttributes().containsKey(attributeName)
            ? roleAssignment.getAttributes().get(attributeName).asText()
            : "null";
    }

}
