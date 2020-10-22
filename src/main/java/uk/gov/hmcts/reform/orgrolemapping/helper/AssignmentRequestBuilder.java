package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashMap;

@Setter
public class AssignmentRequestBuilder {

    public static final String ASSIGNER_ID = "123e4567-e89b-42d3-a456-556642445678";
    public static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    public static final String PROCESS_ID = "staff-organisational-role-mapping";
    public static final String ROLE_NAME_TCW = "tribunal-caseworker";

    private AssignmentRequestBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest(Boolean replaceExisting) {
        return new AssignmentRequest(buildRequest(replaceExisting),
                buildRequestedRoleCollection());
    }

    public static Request buildRequest(Boolean replaceExisting) {

        return Request.builder()
                .assignerId(ASSIGNER_ID)
                .process((PROCESS_ID))
                .replaceExisting(replaceExisting)
                .build();
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection() {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment());
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment() {
        //LocalDateTime timeStamp = LocalDateTime.now()
        return RoleAssignment.builder()
                .actorId(ACTOR_ID)
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .roleName(ROLE_NAME_TCW)
                .classification(Classification.PUBLIC)
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.STAFF)
                .readOnly(false)
                .attributes(JacksonUtils.convertValue(buildAttributesFromFile()))
                .build();
    }

    private static JsonNode buildAttributesFromFile() {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream("attributes.json")) {
            assert inputStream != null;
            JsonNode result = new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
            inputStream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static JsonNode buildAttributesFromFile(String fileName) {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream(fileName)) {
            assert inputStream != null;
            return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static RoleAssignment buildRoleAssignmentForStaff() {
        LocalDateTime timeStamp = LocalDateTime.now();
        return RoleAssignment.builder()
                .actorId("")
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .roleName("")
                .classification(Classification.PUBLIC)
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.STAFF)
                .readOnly(false)
                .attributes(new HashMap<>())
                .build();
    }

    public static List<UserAccessProfile> convertUserProfileToUserAccessProfile(List<UserProfile> userProfiles) {
        //roleId X serviceCode
        List<UserAccessProfile> userAccessProfiles = new ArrayList<>();
        for (UserProfile userProfile : userProfiles) {
            userProfile.getRole().forEach(role -> {
                userProfile.getWorkArea().forEach(workArea -> {
                    UserAccessProfile userAccessProfile = new UserAccessProfile();
                    userAccessProfile.setId(userProfile.getId());
                    userAccessProfile.setDeleteFlag(userProfile.isDeleteFlag());
                    userProfile.getBaseLocation().forEach(baseLocation -> {
                        if (baseLocation.isPrimary()) {
                            userAccessProfile.setPrimaryLocationId(baseLocation.getLocationId());
                            userAccessProfile.setPrimaryLocationName(baseLocation.getLocation());
                        }
                    });
                    userAccessProfile.setAreaOfWorkId(workArea.getAreaOfWork());
                    userAccessProfile.setServiceCode(workArea.getServiceCode());
                    userAccessProfile.setRoleId(role.getRoleId());
                    userAccessProfile.setRoleName(role.getRoleName());

                    userAccessProfiles.add(userAccessProfile);
                });
            });

        }
        return userAccessProfiles;
    }
}
