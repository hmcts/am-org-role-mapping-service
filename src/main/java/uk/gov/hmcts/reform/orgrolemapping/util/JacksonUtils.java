package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import lombok.SneakyThrows;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@Singleton
public class JacksonUtils {

    private JacksonUtils() {
    }



    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build()
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @SneakyThrows
    public static String writeValueAsPrettyJson(Object input) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
    }

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

    public static TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<>() {
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

    public static CaseWorkerProfilesResponse convertInCaseWorkerProfileResponse(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }

    public static List<CaseWorkerProfilesResponse> convertListInCaseWorkerProfileResponse(List<Object> from) {
        List<CaseWorkerProfilesResponse> caseWorkerProfilesResponses = new ArrayList<>();
        for (Object obj : from) {
            caseWorkerProfilesResponses.add(MAPPER.convertValue(obj, new TypeReference<>() {
            }));
        }

        return caseWorkerProfilesResponses;
    }

    public static JudicialProfileV2 convertInJudicialProfileV2(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }

    public static JudicialBooking convertInJudicialBooking(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {
        });
    }

    public static String convertObjectToString(Object from) {
        try {
            return MAPPER.writeValueAsString(from);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error occurred when serializing object");
        }
    }
}
