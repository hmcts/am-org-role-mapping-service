package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public abstract class JsonBConverterTest {

    // Methods to be implemented by concrete test classes
    protected abstract JsonBConverter getSut();

    protected abstract String getJsonNodeFileName();

    protected abstract String getExampleJson();

    @Test
    void convertToDatabaseColumn() throws IOException {
        JsonNode jsonNode = TestDataBuilder.buildJsonNodeFromFile(getJsonNodeFileName());
        String result = getSut().convertToDatabaseColumn(jsonNode);
        assertEquals(jsonNode.toString(), result);
    }

    @Test
    void convertToDatabaseColumn_Null() throws IOException {
        String result = getSut().convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute() throws IOException {
        JsonNode result = getSut().convertToEntityAttribute(getExampleJson());
        assertEquals(TestDataBuilder.buildJsonNodeFromFile(getJsonNodeFileName()), result);
    }

    @Test
    void convertToNullEntityAttribute() throws IOException {
        JsonNode result = getSut().convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void convertWrongJsonToEntityAttribute() throws IOException {
        String wrongJson = getExampleJson().replace(",","");
        JsonNode result = getSut().convertToEntityAttribute(wrongJson);
        assertNull(result);
    }
}
