package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Named
@Singleton
public class JacksonUtils {

    private JacksonUtils() {
    }



    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);



    public static Map<String, JsonNode> convertValue(Object from) {
        return MAPPER.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static AssignmentRequest readValue(String content) throws JsonProcessingException {
        MAPPER.registerModule(new JavaTimeModule());
        return MAPPER.readValue(content, new TypeReference<>() {
        });
    }

    public static JsonNode convertObjectIntoJsonNode(Object from) {
        return MAPPER.convertValue(from, JsonNode.class);
    }

    public static final TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

    public static RoleAssignmentRequestResource convertRoleAssignmentResource(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }

    public static CaseWorkerProfile convertInCaseWorkerProfile(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }

    public static JudicialProfile convertInJudicialProfile(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }
}
