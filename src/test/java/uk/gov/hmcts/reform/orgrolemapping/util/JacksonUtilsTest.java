package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
class JacksonUtilsTest {

    @Test
    void convertValue() {
        assertNotNull(JacksonUtils.convertValue(TestDataBuilder.buildAttributesFromFile()));
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
}