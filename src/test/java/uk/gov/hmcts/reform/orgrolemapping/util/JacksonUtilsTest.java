package uk.gov.hmcts.reform.orgrolemapping.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
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
        JudicialProfile judicialProfile = TestDataBuilder.buildJudicialProfile();
        assertNotNull(JacksonUtils.convertInJudicialProfileV2(judicialProfile));
    }

    @Test
    void convertInRoleAssignmentResource() {
        RoleAssignmentRequestResource from = new RoleAssignmentRequestResource(AssignmentRequest.builder().build());
        assertNotNull(JacksonUtils.convertRoleAssignmentResource(from));
    }

    @Test
    void convertInCaseWorkerProfileResponse() {
        CaseWorkerProfilesResponse from = new CaseWorkerProfilesResponse("",
                CaseWorkerProfile.builder().build());
        assertNotNull(JacksonUtils.convertInCaseWorkerProfileResponse(from));
    }

    @Test
    void convertInJudicialBookings() throws IOException {
        JudicialBooking judicialBooking = TestDataBuilder.buildJudicialBooking();
        assertNotNull(JacksonUtils.convertInJudicialBooking(judicialBooking));
    }

    @Test
    void convertUserAccessTypes() throws IOException {

        String exampleRequest = new String(
                Files.readAllBytes(Paths.get("src/main/resources/userAccessType.json")));
        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(exampleRequest);
        assertNotNull(userAccessTypes);
        assertEquals("CIVIL", userAccessTypes.get(0).getJurisdictionId());
        assertEquals("SOLICITOR_PROFILE", userAccessTypes.get(0).getOrganisationProfileId());
        assertEquals("CIVIL_ACCESS_TYPE_ID", userAccessTypes.get(0).getAccessTypeId());
        assertEquals(true, userAccessTypes.get(0).getEnabled());
    }

}
