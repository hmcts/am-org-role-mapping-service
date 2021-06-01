package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Setter
@Slf4j
public class AssignmentRequestBuilder {

    public static final String ASSIGNER_ID = "123e4567-e89b-42d3-a456-556642445678";
    public static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    public static final String PROCESS_ID = "staff-organisational-role-mapping";
    public static final String ROLE_NAME_TCW = "tribunal-caseworker";
    public static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";

    private AssignmentRequestBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest(Boolean replaceExisting) {
        return new AssignmentRequest(buildRequest(replaceExisting),
                buildRequestedRoleCollection());
    }

    public static Request buildRequest(Boolean replaceExisting) {

        return Request.builder()
                .assignerId(ASSIGNER_ID) // This won't be set for the requests.
                .process((PROCESS_ID))
                .requestType(RequestType.CREATE)
                .correlationId(UUID.randomUUID().toString())
                .replaceExisting(replaceExisting)
                .build();
    }

    public static Collection<RoleAssignment> buildRequestedRoleCollection() {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildRoleAssignment());
        return requestedRoles;
    }

    public static RoleAssignment buildRoleAssignment() {
        return RoleAssignment.builder()
                .actorId(ACTOR_ID)
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .roleName(ROLE_NAME_TCW)
                .classification(Classification.PUBLIC)
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.LEGAL_OPERATIONS)
                .readOnly(false)
                .attributes(JacksonUtils.convertValue(buildAttributesFromFile("attributes.json")))
                .build();
    }

    public static JsonNode buildAttributesFromFile(String fileName) {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream(fileName)) {
            assert inputStream != null;
            return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });

        } catch (Exception e) {
            throw new InvalidRequest("Class cast exception while parsing the json file");
        }
    }

    public static RoleAssignment buildRequestedRoleForStaff() {
        return RoleAssignment.builder()
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .classification(Classification.PUBLIC) //default is public
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.LEGAL_OPERATIONS)
                .readOnly(false)
                .attributes(new HashMap<>())
                .build();
    }

    public static Set<UserAccessProfile> convertUserProfileToUserAccessProfile(UserProfile userProfile) {
        long startTime = System.currentTimeMillis();
        //roleId X serviceCode
        Set<UserAccessProfile> userAccessProfiles = new HashSet<>();

        userProfile.getRole().forEach(role ->
            userProfile.getWorkArea().forEach(workArea -> {
                UserAccessProfile userAccessProfile = new UserAccessProfile();
                userAccessProfile.setId(userProfile.getId());
                userAccessProfile.setSuspended(userProfile.isSuspended());
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
            })
        );

        log.debug("Execution time of convertUserProfileToUserAccessProfile() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(),startTime)));
        return userAccessProfiles;
    }
}
