package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

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
    void convertInCaseWorkerProfile() {
        assertNotNull(JacksonUtils.convertInCaseWorkerProfile(""));
    }

    @Test
    void convertInJudicialProfile() {
        assertNotNull(JacksonUtils.convertInJudicialProfile("\"elinkId\": \"21336a2b-79ce-44eb-9168-2d49a744be9c\""));
    }
}