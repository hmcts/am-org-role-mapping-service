package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.RoleAssignmentConstants.Attributes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.PROFESSIONAL_ORGANISATIONAL_ROLE_MAPPING;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertValue;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.getRestructuredAccessTypes;

@Service
@Slf4j
@AllArgsConstructor
public class ProfessionalRefreshOrchestrationHelper {

    public static final String ACCESS_TYPES_VERSION_INVALID
        = "User %s has access types version %d which is higher than the latest version %d";

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private final AccessTypesRepository accessTypesRepository;

    private final RoleAssignmentService roleAssignmentService;

    private final SecurityUtils securityUtils;
    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertUserRefreshQueue(RefreshUser refreshUser) {
        ProfessionalUserData professionalUserData = ProfessionalUserBuilder.fromProfessionalRefreshUser(refreshUser);

        userRefreshQueueRepository.upsert(
            professionalUserData.getUserId(),
            professionalUserData.getUserLastUpdated(),
            getLatestAccessTypes().getVersion(),
            professionalUserData.getDeleted(),
            professionalUserData.getAccessTypes(),
            professionalUserData.getOrganisationId(),
            professionalUserData.getOrganisationStatus(),
            professionalUserData.getOrganisationProfileIds()
        );
    }

    public AccessTypesEntity getLatestAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc().orElseThrow(
            () -> new ServiceException(NO_ACCESS_TYPES_FOUND)
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processActiveUserRefreshQueue(AccessTypesEntity accessTypes) {

        Optional<UserRefreshQueueEntity> userRefreshQueue = userRefreshQueueRepository.findFirstByActiveTrue();

        if (userRefreshQueue.isEmpty()) {
            return;
        }

        refreshSingleUser(userRefreshQueue.get(), accessTypes);
    }

    public void refreshSingleUser(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        log.debug("Refreshing professional role assignments for user '{}'", userRefreshQueue.getUserId());
        generateRoleAssignments(userRefreshQueue, accessTypes);
        userRefreshQueue.setActive(false);
    }

    private void generateRoleAssignments(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        // STEP 1. If user_refresh_queue.access_types_min_version > access_types.version,
        // then abort processing for this user and do not clear the user_refresh_queue record.
        if (!isMinVersionValid(userRefreshQueue, accessTypes)) {
            String errorMessage = String.format(
                    ACCESS_TYPES_VERSION_INVALID,
                    userRefreshQueue.getUserId(),
                    userRefreshQueue.getAccessTypesMinVersion(),
                    accessTypes.getVersion().intValue()
            );
            log.error(errorMessage);
            throw new ServiceException(errorMessage);
        }

        AssignmentRequest assignmentRequest = createAssignmentRequest(userRefreshQueue, accessTypes);
        roleAssignmentService.createRoleAssignment(assignmentRequest);
        log.info("Count of RoleAssignments for {}={}", userRefreshQueue.getUserId(),
                assignmentRequest.getRequestedRoles().size());
    }

    private AssignmentRequest createAssignmentRequest(UserRefreshQueueEntity userRefreshQueue,
                                                      AccessTypesEntity accessTypes) {

        String reference = userRefreshQueue.getUserId();
        List<RoleAssignment> usersRoleAssignments;
        try {
            usersRoleAssignments = prepareRoleAssignments(userRefreshQueue, accessTypes);
        } catch (JsonProcessingException e) {
            throw new ServiceException("There was a problem creating an assignment Request", e);
        }

        AssignmentRequest assignmentRequest;

        assignmentRequest = AssignmentRequest.builder()
                .request(
                        Request.builder()
                                .requestType(RequestType.CREATE)
                                .replaceExisting(true)
                                .process(PROFESSIONAL_ORGANISATIONAL_ROLE_MAPPING)
                                .reference(reference)
                                .assignerId(securityUtils.getUserId())
                                .clientId(AM_ORG_ROLE_MAPPING_SERVICE)
                                .correlationId(UUID.randomUUID().toString())
                                .build())
                .requestedRoles(usersRoleAssignments)
                .build();

        return assignmentRequest;
    }

    private List<RoleAssignment> prepareRoleAssignments(UserRefreshQueueEntity userRefreshQueue,
                                                        AccessTypesEntity accessTypes) throws JsonProcessingException {
        // STEP 2. If user_refresh_queue.deleted != null then halt and return an empty set of role assignments.
        if (isUserRefreshQueueDeleted(userRefreshQueue)) {
            return Collections.emptyList();
        }

        // STEP 3. If user_refresh_queue.organisation_status is not an active status, then halt and return an empty
        // set of role assignments.
        if (!isOrganisationStatusActive(userRefreshQueue.getOrganisationStatus())) {
            return Collections.emptyList();
        }

        // Create a map of the userAccessTypes from PRM.
        Map<String, Map<String, List<UserAccessType>>> userAccessTypeMap = buildUserAccessTypeMap(userRefreshQueue);

        // STEP 4:- Filter access_types to contain only data for the organisation profiles in
        // user_refresh_queue.organisation_profile_ids.
        RestructuredAccessTypes ccdAccessTypes = getRestructuredAccessTypes(accessTypes.getAccessTypes());
        Map<String, Map<String, List<OrganisationProfileAccessType>>> ccdAccessTypeMap =
                buildAccessTypeMap(ccdAccessTypes, userRefreshQueue);

        // STEP 5: Validate the remaining accessTypes against the rules.
        Map<String, List<OrganisationProfileAccessType>> validatedAccessTypesMap =
                buildValidatedAccessTypesMap(userAccessTypeMap, ccdAccessTypeMap);

        // STEP 6. Process the access type role records for each remaining access type record.
        Set<RoleAssignment> roleAssignments = new HashSet<>();
        validatedAccessTypesMap.forEach((jurisdictionId, organisationProfileAccessTypes) ->
                organisationProfileAccessTypes.forEach(accessType -> accessType.getRoles()
                        .forEach(role -> roleAssignments.addAll(
                                        createRoleAssignmentsForRole(role, jurisdictionId, userRefreshQueue)))));

        // STEP 7. Deduplicate the role assignments. (...by using a Set rather than a list).
        return roleAssignments.stream().toList();
    }

    protected Map<String, Map<String, List<UserAccessType>>> buildUserAccessTypeMap(
            UserRefreshQueueEntity userRefreshQueue) throws JsonProcessingException {

        // Get the list of access types.
        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(userRefreshQueue.getAccessTypes());
        Map<String, Map<String, List<UserAccessType>>> map = new HashMap<>();

        // Add the userAccessTypes, nested by organisationProfileId and jurisdictionId.
        userAccessTypes.forEach(userAccessType ->
            // Add the OrganisationProfileId.
            map.computeIfAbsent(userAccessType.getOrganisationProfileId(), k -> new HashMap<>())
                // Add the jurisdictionId.
                .computeIfAbsent(userAccessType.getJurisdictionId(), k -> new ArrayList<>())
                // Add the accessType.
                .add(userAccessType)
        );
        return map;
    }

    /**
     * STEP 4:-
     * Filter access_types to contain only data for the organisation profiles in
     * user_refresh_queue.organisation_profile_ids.
     */
    protected Map<String, Map<String, List<OrganisationProfileAccessType>>> buildAccessTypeMap(
            RestructuredAccessTypes ccdAccessTypes,
            UserRefreshQueueEntity userRefreshQueue) {
        // Get the list of user's organisationProfileIds.
        List<String> usersOrganisationProfileIdList = getUsersOrganisationProfileIdList(userRefreshQueue);
        Map<String, Map<String, List<OrganisationProfileAccessType>>> map = new HashMap<>();

        // Loop the ccdAccessTypes (filtering organisationProfileIds against user's organisationProfileIds as we go)
        ccdAccessTypes.getOrganisationProfiles().stream()
            .filter(organisationProfile ->
                usersOrganisationProfileIdList.contains(organisationProfile.getOrganisationProfileId())
            )
            .forEach(organisationProfile ->

                // loop the jurisdictions
                organisationProfile.getJurisdictions().forEach(jurisdiction ->

                    // Loop the accessTypes
                    jurisdiction.getAccessTypes().forEach(accessType ->

                        // Add the OrganisationProfileId.
                        map.computeIfAbsent(organisationProfile.getOrganisationProfileId(),
                                            jurisdictionIds -> new HashMap<>())
                            // Add the jurisdictionId.
                            .computeIfAbsent(jurisdiction.getJurisdictionId(), accessTypes -> new ArrayList<>())
                                // Add the accessTypes to the jurisdiction map.
                                .add(accessType)
                    )
                )
        );
        return map;
    }

    /**
     *  STEP 5:-
     *  a. For each remaining access type record, extract the corresponding user_refresh_queue
     *     access_types record, matching on jurisdiction ID, organisation profile ID and access type ID.
     *  b. Filter the remaining access type records (access_type) using their corresponding
     *     user_access_type records: where AccessType is valid for user (see rule).
     */
    private Map<String, List<OrganisationProfileAccessType>> buildValidatedAccessTypesMap(
            Map<String, Map<String, List<UserAccessType>>> userAccessTypeMap,
            Map<String, Map<String, List<OrganisationProfileAccessType>>> ccdAccessTypeMap) {
        Map<String, List<OrganisationProfileAccessType>> map = new HashMap<>();

        // Loop the organisationProfileIds
        ccdAccessTypeMap.forEach((organisationProfileId, jurisdictionMap) ->

            // Loop the jurisdictionIds
            jurisdictionMap.forEach((jurisdictionId, ccdAccessTypes) ->

                // Loop the accessTypes
                ccdAccessTypes.forEach(ccdAccessType -> {

                    // STEP 5a: ... extract the corresponding user_access_type record:
                    // matching on jurisdiction ID, organisation profile ID and access type ID.
                    UserAccessType userAccessType = findUserAccessTypeInMap(userAccessTypeMap,
                            organisationProfileId, jurisdictionId, ccdAccessType.getAccessTypeId());

                    // STEP 5b: Filter the remaining access type records (access_type) using their
                    // corresponding user_access_type records: where AccessType is valid for user (see rule).
                    if (isAccessTypeValid(ccdAccessType, userAccessType)) {
                        // Add the validated accessType to the result map.
                        map.computeIfAbsent(jurisdictionId, k -> new ArrayList<>())
                            .add(ccdAccessType);
                    }
                })
            )
        );
        return map;
    }

    protected UserAccessType findUserAccessTypeInMap(Map<String, Map<String, List<UserAccessType>>> userAccessTypeMap,
                                                     String organisationProfileId,
                                                     String jurisdictionId,
                                                     String accessTypeId) {

        UserAccessType result = null;
        if (userAccessTypeMap.containsKey(organisationProfileId)
            && userAccessTypeMap.get(organisationProfileId).containsKey(jurisdictionId)
            && StringUtils.isNotBlank(accessTypeId)
        ) {
            // Get the accessType from the map
            result = userAccessTypeMap.get(organisationProfileId).get(jurisdictionId)
                    .stream()
                    .filter(userAccessType -> accessTypeId.equals(userAccessType.getAccessTypeId()))
                    .findFirst()
                    .orElse(null);
        }
        return result;
    }

    private List<RoleAssignment> createRoleAssignmentsForRole(AccessTypeRole role,
                                                              String jurisdictionId,
                                                              UserRefreshQueueEntity userRefreshQueue) {
        List<RoleAssignment> roleAssignments =  new ArrayList<>();

        // if contains OrganisationalRole config
        String organisationalRoleName = role.getOrganisationalRoleName();
        if (StringUtils.isNotBlank(organisationalRoleName)) {
            roleAssignments.add(createRoleAssignment(organisationalRoleName, userRefreshQueue.getUserId(),
                    jurisdictionId, role.getCaseTypeId(), null));
        }

        // if contains GroupRole config (and is GA enabled)
        String groupRoleName = role.getGroupRoleName();
        String caseGroupIdTemplate = role.getCaseGroupIdTemplate();
        if (role.isGroupAccessEnabled()
                && StringUtils.isNotBlank(groupRoleName)
                && StringUtils.isNotBlank(caseGroupIdTemplate)) {

            String caseAccessGroupId =
                    generateCaseAccessGroupId(caseGroupIdTemplate, userRefreshQueue.getOrganisationId());
            roleAssignments.add(createRoleAssignment(groupRoleName, userRefreshQueue.getUserId(),
                    jurisdictionId, role.getCaseTypeId(), caseAccessGroupId));
        }

        return roleAssignments;
    }

    protected boolean isAccessTypeValid(OrganisationProfileAccessType accessType,
                                        UserAccessType userAccessType) {
        // STEP 5b: Retain records where:
        //
        //    (access_type.access_mandatory = true)
        // OR
        //    (user_access_type == null AND access_type.access_default = true)
        // OR
        //    (user_access_type.enabled = true)
        return isAccessTypeMandatory(accessType)
                || isAccessTypeDefaulted(accessType, userAccessType)
                || isAccessTypeEnabled(userAccessType);
    }

    protected boolean isAccessTypeMandatory(OrganisationProfileAccessType accessType) {
        return accessType.isAccessMandatory();
    }

    protected boolean isAccessTypeDefaulted(OrganisationProfileAccessType accessType,
                                            UserAccessType userAccessType) {
        return accessType.isAccessDefault() && userAccessType == null;
    }

    protected boolean isAccessTypeEnabled(UserAccessType accessType) {
        return accessType != null && Boolean.TRUE.equals(accessType.getEnabled());
    }

    private RoleAssignment createRoleAssignment(String roleName, String userId, String jurisdictionId,
                                                String caseTypeId, String caseAccessGroupId) {

        Map<String, String> attributes = new HashMap<>();

        attributes.put(Attributes.Name.JURISDICTION, jurisdictionId);
        attributes.put(Attributes.Name.CASE_TYPE, caseTypeId);
        if (null != caseAccessGroupId) {
            attributes.put(Attributes.Name.CASE_ACCESS_GROUP_ID, caseAccessGroupId);
        }

        return RoleAssignment.builder()
                .actorId(userId)
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .roleName(roleName)
                .roleCategory(RoleCategory.PROFESSIONAL)
                .classification(Classification.RESTRICTED)
                .grantType(GrantType.STANDARD)
                .authorisations(Collections.emptyList())
                .readOnly(false)
                .status(CREATE_REQUESTED)
                .attributes(convertValue(attributes))
                .build();
    }

    private String generateCaseAccessGroupId(String caseGroupIdTemplate, String organisationId) {
        return caseGroupIdTemplate.replace("$ORGID$", organisationId);
    }

    private List<String> getUsersOrganisationProfileIdList(UserRefreshQueueEntity userRefreshQueue) {
        List<String> organisationProfileIdList = List.of();
        if (userRefreshQueue.getOrganisationProfileIds() != null) {
            organisationProfileIdList = Arrays.stream(userRefreshQueue.getOrganisationProfileIds()).toList();
        }
        return organisationProfileIdList;
    }

    private boolean isMinVersionValid(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        return userRefreshQueue.getAccessTypesMinVersion() <= accessTypes.getVersion().intValue();
    }

    private boolean isOrganisationStatusActive(String organisationStatus) {
        return OrganisationStatus.valueOf(organisationStatus).isActive();
    }

    private boolean isUserRefreshQueueDeleted(UserRefreshQueueEntity userRefreshQueue) {
        return null != userRefreshQueue.getDeleted();
    }

}
