package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

@Named
@Singleton
public class JacksonUtils {

    private JacksonUtils() {
    }

    public static final JsonFactory jsonFactory = JsonFactory.builder()
        // Change per-factory setting to prevent use of `String.intern()` on symbols
        .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
        .build();

    public static final ObjectMapper MAPPER = JsonMapper.builder(jsonFactory)
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .build();



    public static HashMap<String, JsonNode> convertValue(Object from) {
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

    public AssignmentRequest convertUserProfileToAssignmentRequest(UserProfile request) {

        Collection<RoleAssignment> roleAssignmentCollections = Collections.singletonList(
                RoleAssignment.builder()
                .actorId(request.getId())
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.CASE)
                .roleName(request.getRole().get(0).getRoleName())
                .roleCategory(RoleCategory.JUDICIAL)
                .classification(Classification.RESTRICTED)
                //.attributes(null)
                .grantType(GrantType.SPECIFIC)
                .readOnly(false)
                .build());

        return AssignmentRequest.builder()
                .request(Request.builder()
                        .assignerId(UUID.randomUUID().toString())
                        .replaceExisting(true)
                        .build())
                .requestedRoles(roleAssignmentCollections)
                .build();

    }
}
