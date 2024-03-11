package uk.gov.hmcts.reform.orgrolemapping.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

@RunWith(MockitoJUnitRunner.class)
class JacksonUtilsTest {

    @Test
    void convertValue() {
        Map<String, JsonNode> jsonNodeMap = JacksonUtils.convertValue(TestDataBuilder.buildAttributesFromFile());
        assertNotNull(jsonNodeMap);
        assertEquals("123456",jsonNodeMap.get("primaryLocation").asText());
        assertEquals("IA",jsonNodeMap.get("jurisdiction").asText());
    }

    @Test
    void convertObjectIntoJsonNode() {
        assertNotNull(JacksonUtils.convertObjectIntoJsonNode("\"id\": \"21334a2b-79ce-44eb-9168-2d49a744be9c\""));
    }

    @Test
    void getHashMapTypeReference() {
        assertNotNull(JacksonUtils.getHashMapTypeReference());
    }

    @Test
    void convertInCaseWorkerProfile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CaseWorkerProfile caseWorkerProfile =
                objectMapper.readValue(new File("src/main/resources/userProfileSample.json"),
                        CaseWorkerProfile.class);
        assertNotNull(JacksonUtils.convertInCaseWorkerProfile(caseWorkerProfile));
    }

    @Test
    void convertInJudicialProfile() throws IOException {
        JudicialProfile judicialProfile = TestDataBuilder.buildJudicialProfile();
        assertNotNull(JacksonUtils.convertInJudicialProfile(judicialProfile));
    }

    @Test
    void convertInJudicialProfileV2() throws IOException {

        // GIVEN
        JudicialProfileV2 judicialProfileV2 = TestDataBuilder.buildJudicialProfileV2();

        // WHEN
        JudicialProfileV2 response = (JacksonUtils.convertInJudicialProfileV2(judicialProfileV2));

        // THEN
        assertNotNull(response);

    }

    @Test
    void convertListInJudicialProfileV2() throws IOException {

        // GIVEN
        JudicialProfileV2 judicialProfileV2 = TestDataBuilder.buildJudicialProfileV2();

        // WHEN
        List<JudicialProfileV2> response = (JacksonUtils.convertListInJudicialProfileV2(List.of(judicialProfileV2)));

        // THEN
        assertNotNull(response);
        assertTrue(response.size() > 0);

    }

    @Test
    void convertListInJudicialProfileV2_empty() {

        // WHEN
        List<JudicialProfileV2> response = (JacksonUtils.convertListInJudicialProfileV2(List.of()));

        // THEN
        assertNotNull(response);
        assertEquals(0, response.size());

    }

    @Test
    void convertInRoleAssignmentResource() {
        RoleAssignmentRequestResource from = new RoleAssignmentRequestResource(AssignmentRequest.builder().build());
        assertNotNull(JacksonUtils.convertRoleAssignmentResource(from));
    }

    @Test
    void convertInCaseWorkerProfileResponse() {

        // GIVEN
        CaseWorkerProfilesResponse from = new CaseWorkerProfilesResponse("",
                CaseWorkerProfile.builder().build());

        // WHEN
        var response = JacksonUtils.convertInCaseWorkerProfileResponse(from);

        // THEN
        assertNotNull(response);
    }

    @Test
    void convertListInCaseWorkerProfileResponse() {

        // GIVEN
        CaseWorkerProfilesResponse from = new CaseWorkerProfilesResponse("",
                CaseWorkerProfile.builder().build());

        // WHEN
        List<CaseWorkerProfilesResponse> response = JacksonUtils.convertListInCaseWorkerProfileResponse(List.of(from));

        // THEN
        assertNotNull(response);
        assertTrue(response.size() > 0);

    }

    @Test
    void convertListInCaseWorkerProfileResponse_empty() {

        // WHEN
        List<CaseWorkerProfilesResponse> response = JacksonUtils.convertListInCaseWorkerProfileResponse(List.of());

        // THEN
        assertNotNull(response);
        assertEquals(0, response.size());

    }

    @Test
    void convertInJudicialBookings() throws IOException {
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        assertNotNull(JacksonUtils.convertInJudicialBooking(judicialBooking));
    }
}