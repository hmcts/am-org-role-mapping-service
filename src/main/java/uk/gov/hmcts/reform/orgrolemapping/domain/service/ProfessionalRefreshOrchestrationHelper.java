package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
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


    private AccessTypesEntity getLatestAccessTypes() {
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
        if (isMinVersionExpired(userRefreshQueue, accessTypes)) {
            String errorMessage = String.format(
                    "User %s has access types version %d which is higher than the latest version %d",
                    userRefreshQueue.getUserId(),
                    userRefreshQueue.getAccessTypesMinVersion(),
                    accessTypes.getVersion().intValue()
            );
            log.error(errorMessage);
            throw new ServiceException(errorMessage);
        }
        AssignmentRequest assignmentRequest =
                createAssignmentRequest(userRefreshQueue, accessTypes);
        ResponseEntity<Object> responseEntity = roleAssignmentService.createRoleAssignment(assignmentRequest);
        log.info("Count of RoleAssignments for {}={}", userRefreshQueue.getUserId(),
                assignmentRequest.getRequestedRoles().size());
    }

    protected boolean isMinVersionExpired(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        return userRefreshQueue.getAccessTypesMinVersion() > accessTypes.getVersion().intValue();
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

        RestructuredAccessTypes prmRestructuredAccessTypes =
                getRestructuredAccessTypes(accessTypes.getAccessTypes());

        Set<OrganisationProfile> organisationProfiles = prmRestructuredAccessTypes.getOrganisationProfiles();

        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(userRefreshQueue.getAccessTypes());

        // Create a map of the userAccessTypes from PRD.
        Map<String, Map<String, List<UserAccessType>>> userAccessTypeMap =
                buildUserAccessTypeMap(Arrays.stream(userRefreshQueue.getOrganisationProfileIds()).toList(),
                        userAccessTypes);

        // Create a filtered map of accessTypes from CCD (matched against the userAccessTypesMap).
        Map<String, Map<String, List<OrganisationProfileAccessType>>> accessTypeMap =
                buildAccessTypeMap(userAccessTypeMap, organisationProfiles);

        // Validate the remaining accessTypes against the rules.
        Map<String, List<OrganisationProfileAccessType>> validatedAccessTypesMap =
                buildValidatedAccessTypesMap(userAccessTypeMap, accessTypeMap);

        // STEP 6. Process the access type role records for each remaining access type record.
        Set<RoleAssignment> roleAssignments = new HashSet<>();
        validatedAccessTypesMap.forEach((jurisdictionId, organisationProfileAccessTypes) ->
                organisationProfileAccessTypes.forEach(accessType -> accessType.getRoles()
                        .forEach(role -> roleAssignments.addAll(
                                        createRoleAssignmentsForRole(role, jurisdictionId, userRefreshQueue)))));

        // STEP 7. Deduplicate the role assignments. (...by using a Set rather than a list).
        return roleAssignments.stream().toList();
    }

    private boolean isUserRefreshQueueDeleted(UserRefreshQueueEntity userRefreshQueue) {
        return null != userRefreshQueue.getDeleted();
    }

    protected Map<String, Map<String, List<UserAccessType>>> buildUserAccessTypeMap(
            List<String> organisationProfileIdsList,
            List<UserAccessType> userAccessTypes) {
        Map<String, Map<String, List<UserAccessType>>> map = new HashMap<>();
        // Add the userRefreshQueue.organisationProfileIds
        organisationProfileIdsList.forEach(organizationProfileId ->
                map.computeIfAbsent(organizationProfileId, k -> new HashMap<>()));
        // Add the userAccessTypes, nested by organisationProfileId and jurisdictionId.
        userAccessTypes.stream()
            .filter(userAccessType ->
                    organisationProfileIdsList.contains(userAccessType.getOrganisationProfileId()))
            .forEach(userAccessType ->
            // Add the OranisationProfileId.
            map.get(userAccessType.getOrganisationProfileId())
                // Add the jurisdictionId.
                .computeIfAbsent(userAccessType.getJurisdictionId(), k -> new ArrayList<>())
                // Add the accessType.
                .add(userAccessType));
        return map;
    }

    protected Map<String, Map<String, List<OrganisationProfileAccessType>>> buildAccessTypeMap(
            Map<String, Map<String, List<UserAccessType>>> userAccessMap,
            Set<OrganisationProfile> organisationProfiles) {
        Map<String, Map<String, List<OrganisationProfileAccessType>>> map = new HashMap<>();
        organisationProfiles.stream()

            // STEP 4. Filter access_types to contain only data for the organisation profiles in
            // user_refresh_queue.organisation_profile_ids.

            .filter(organisationProfile ->
                    userAccessMap.containsKey(organisationProfile.getOrganisationProfileId()))
            .forEach(organisationProfile -> {

                // STEP 5a. For each remaining access type record, extract the corresponding user_refresh_queue
                // access_types record, matching on jurisdiction ID, organisation profile ID and access type ID.

                // Add the OrganisatioinProfileId.
                map.computeIfAbsent(organisationProfile.getOrganisationProfileId(),
                        jurisdictionIds -> new HashMap<>());

                organisationProfile.getJurisdictions().stream()
                    .filter(jurisdiction ->
                            userAccessMap.get(organisationProfile.getOrganisationProfileId())
                                    .containsKey(jurisdiction.getJurisdictionId()))
                    .forEach(jurisdiction -> {

                        // Add the jurisdictionId.
                        map.get(organisationProfile.getOrganisationProfileId())
                                .computeIfAbsent(jurisdiction.getJurisdictionId(),
                                        accessTypes -> new ArrayList<>());

                        jurisdiction.getAccessTypes().stream()
                            .filter(accessType ->
                                userAccessMap.get(organisationProfile.getOrganisationProfileId())
                                    .get(jurisdiction.getJurisdictionId()).stream()
                                    .anyMatch(userAccessType ->
                                            userAccessType.getAccessTypeId().equals(accessType.getAccessTypeId())))
                            .forEach(accessType ->

                                // Add the accessType.
                                map.computeIfAbsent(organisationProfile.getOrganisationProfileId(),
                                                jurisdictionIds -> new HashMap<>())
                                        .computeIfAbsent(jurisdiction.getJurisdictionId(),
                                                accessTypes -> new ArrayList<>())
                                        .add(accessType)
                        );
                    });
            });
        return map;
    }

    private Map<String, List<OrganisationProfileAccessType>> buildValidatedAccessTypesMap(
            Map<String, Map<String, List<UserAccessType>>> userAccessTypeMap,
            Map<String, Map<String, List<OrganisationProfileAccessType>>> accessTypeMap) {
        Map<String, List<OrganisationProfileAccessType>> map = new HashMap<>();
        // Loop the organisationProfileIds
        accessTypeMap.forEach((organisationProfileId, jurisdictionMap) ->

            // Loop the jurisdictionIds
            jurisdictionMap.forEach((jurisdictionId, accessTypes) ->

                // Loop the accessTypes
                accessTypes.forEach(accessType -> {

                    // Get the corresponding userAccessTypes for the organisationProfile and Jurisdiction.
                    List<UserAccessType> userAccessTypes =
                            userAccessTypeMap.get(organisationProfileId).get(jurisdictionId);

                    // STEP 5b. Filter the remaining access type records (access_type) using their corresponding
                    // user_access_type records.
                    if (isAccessTypeValid(accessType, userAccessTypes)) {
                        map.computeIfAbsent(jurisdictionId, k -> new ArrayList<>())
                                .add(accessType);
                    }
                })
            )
        );
        return map;
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
                                        List<UserAccessType> userAccessTypes) {
        return isAccessTypeMandatory(accessType)
                || isAccessTypeDefaulted(accessType, userAccessTypes)
                || isAccessTypeEnabled(userAccessTypes);
    }

    protected boolean isAccessTypeMandatory(OrganisationProfileAccessType accessType) {
        return accessType.isAccessMandatory();
    }

    protected boolean isAccessTypeDefaulted(OrganisationProfileAccessType accessType,
                                            List<UserAccessType> userAccessTypes) {
        return accessType.isAccessDefault() && CollectionUtils.isEmpty(userAccessTypes);
    }

    protected boolean isAccessTypeEnabled(List<UserAccessType> accessTypes) {
        return accessTypes != null && accessTypes.stream()
                .anyMatch(userAccessType -> Boolean.TRUE.equals(userAccessType.getEnabled()));
    }

    private RoleAssignment createRoleAssignment(String roleName, String userId, String jurisdictionId,
                                                String caseTypeId, String caseAccessGroupId) {

        Map<String, String> attributes = new HashMap<>();

        attributes.put("jurisdiction", jurisdictionId);
        attributes.put("caseType", caseTypeId);
        if (null != caseAccessGroupId) {
            attributes.put("caseAccessGroupId", caseAccessGroupId);
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

    private boolean isOrganisationStatusActive(String organisationStatus) {
        return OrganisationStatus.valueOf(organisationStatus).isActive();
    }

}
