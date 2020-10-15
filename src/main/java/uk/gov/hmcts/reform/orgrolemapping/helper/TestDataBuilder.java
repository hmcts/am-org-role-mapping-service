package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Setter
public class TestDataBuilder {

    private TestDataBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest(Boolean replaceExisting) {
        return new AssignmentRequest(buildRequest(replaceExisting),
                buildRequestedRoleCollection());
    }

    public static Request buildRequest(Boolean replaceExisting) {

        return Request.builder()
                .assignerId("123e4567-e89b-42d3-a456-556642445678")
                .reference("p2")
                .process(("p2"))
                .replaceExisting(replaceExisting)
                .build();
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection() {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment());
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignment.builder()
                .actorId("123e4567-e89b-42d3-a456-556642445612")
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.CASE)
                .roleName("judge")
                .classification(Classification.PUBLIC)
                .grantType(GrantType.SPECIFIC)
                .roleCategory(RoleCategory.JUDICIAL)
                .readOnly(false)
                .attributes(JacksonUtils.convertValue(buildAttributesFromFile()))
                .build();
    }

    private static JsonNode buildAttributesFromFile() {
        try (InputStream inputStream =
                     TestDataBuilder.class.getClassLoader().getResourceAsStream("attributes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode buildNotesFromFile() {
        try (InputStream inputStream =
                     TestDataBuilder.class.getClassLoader().getResourceAsStream("notes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
