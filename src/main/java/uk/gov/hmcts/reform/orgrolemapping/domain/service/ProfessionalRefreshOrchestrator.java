package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SUCCESS_ROLE_REFRESH;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.PROFESSIONAL_ORGANISATIONAL_ROLE_MAPPING;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInRestructuredAccessTypes;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertValue;

@Service
@Slf4j
public class ProfessionalRefreshOrchestrator {

    public static final String NO_ACCESS_TYPES_FOUND = "No access types found in database";
    public static final String PRD_USER_NOT_FOUND = "User with ID %s not found in PRD";
    public static final String EXPECTED_SINGLE_PRD_USER = "Expected single user for ID %s, found %s";

    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";

    protected static final String[] ORGANISATION_ACTIVE_STATUSES =  { "abc", "def", "ghi", "xyz","OrgStatus" };
    private final AccessTypesRepository accessTypesRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final PRDService prdService;
    private final ObjectMapper objectMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final SecurityUtils securityUtils;

    public ProfessionalRefreshOrchestrator(AccessTypesRepository accessTypesRepository,
                                           UserRefreshQueueRepository userRefreshQueueRepository,
                                           PRDService prdService,
                                           ObjectMapper objectMapper,
                                           RoleAssignmentService roleAssignmentService,
                                           SecurityUtils securityUtils) {
        this.accessTypesRepository = accessTypesRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.prdService = prdService;
        this.objectMapper = objectMapper;
        this.roleAssignmentService = roleAssignmentService;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public ResponseEntity<Object> refreshProfessionalUser(String userId) {
        log.info("Single User refreshProfessionalUser for {userid}",userId);
        GetRefreshUsersResponse getRefreshUsersResponse;
        try {
            getRefreshUsersResponse = Objects.requireNonNull(prdService.getRefreshUser(userId).getBody());
        } catch (FeignException.NotFound feignClientException) {
            throw new ResourceNotFoundException(String.format(PRD_USER_NOT_FOUND, userId));
        }

        if (getRefreshUsersResponse.getUsers().size() > 1) {
            throw new ServiceException(String.format(EXPECTED_SINGLE_PRD_USER, userId,
                getRefreshUsersResponse.getUsers().size()));
        }

        this.upsertUserRefreshQueue(getRefreshUsersResponse.getUsers().get(0));

        refreshSingleUser(userRefreshQueueRepository.findByUserId(userId), getLatestAccessTypes());

        return ResponseEntity.ok().body(Map.of("Message", SUCCESS_ROLE_REFRESH));
    }

    @Transactional
    public void refreshProfessionalUsers() {

        this.processActiveUserRefreshQueue(getLatestAccessTypes());

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertUserRefreshQueue(RefreshUser prdUser) {
        String userAccessTypes = null;
        try {
            userAccessTypes = objectMapper.writeValueAsString(prdUser.getUserAccessTypes());
        } catch (JsonProcessingException e) {
            throw new ServiceException(String.format("Unable to serialize user access types for PRD user %s",
                    prdUser.getUserIdentifier()), e);
        }

        userRefreshQueueRepository.upsert(
            prdUser.getUserIdentifier(),
            prdUser.getLastUpdated(),
            getLatestAccessTypes().getVersion(),
            prdUser.getDateTimeDeleted(),
            userAccessTypes,
            prdUser.getOrganisationInfo().getOrganisationIdentifier(),
            prdUser.getOrganisationInfo().getStatus().name(),
            String.join(",", prdUser.getOrganisationInfo().getOrganisationProfileIds())
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processActiveUserRefreshQueue(AccessTypesEntity accessTypes) {

        Optional<UserRefreshQueueEntity> userRefreshQueue = userRefreshQueueRepository.findFirstByActiveTrue();

        if (userRefreshQueue.isEmpty()) {
            return;
        }

        refreshSingleUser(userRefreshQueue.get(), accessTypes);

        // Process any further record(s) recursively
        //processActiveUserRefreshQueue(accessTypes);
    }

    private AccessTypesEntity getLatestAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc().orElseThrow(
            () -> new ServiceException(NO_ACCESS_TYPES_FOUND)
        );
    }

    private void refreshSingleUser(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        log.debug("Refreshing professional role assignments for user '{}'", userRefreshQueue.getUserId());
        generateRoleAssignments(userRefreshQueue, accessTypes);
        userRefreshQueue.setActive(false);
    }

    private void generateRoleAssignments(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        //Step 1 If user_refresh_queue.access_types_min_version > PRM access_types.version,
        //        THEN abort processing for this user and do not clear the user_refresh_queue record.
        //NoteThis will be swept up in a later run.  An alternative would be to re-retrieve the access types data from
        // the PRM database, since it must exist there at a usable version.
        if (userRefreshQueue.getAccessTypesMinVersion() > accessTypes.getVersion()) {
            return;
        }
        AssignmentRequest assignmentRequest =
                createAssignmentRequest(userRefreshQueue,accessTypes);
        ResponseEntity<Object> responseEntity = roleAssignmentService.createRoleAssignment(assignmentRequest);
        log.info("generateRoleAssignments responseEntity" + responseEntity);
    }

    private AssignmentRequest createAssignmentRequest(UserRefreshQueueEntity userRefreshQueue,
                                                            AccessTypesEntity accessTypes) {

        String reference  = userRefreshQueue.getUserId();
        List<RoleAssignment> usersRoleAssignments;
        try {
            usersRoleAssignments = prepareRoleAssignments(userRefreshQueue,accessTypes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
        Set<RoleAssignment> usersRoleAssignments = new HashSet<>();
        // Step 2 If user_refresh_queue.deleted != null then halt and return an empty set of role assignments.
        if (null != userRefreshQueue.getDeleted()) {
            return usersRoleAssignments.stream().toList();
        }

        // Step 3 If user_refresh_queue.organisation_status is not an active status, then halt and return an empty set
        // of role assignments. Note The set of active statuses will have to be provided by PRD team / product owner.

        if (!isOrganisationStatusActive(userRefreshQueue.getOrganisationStatus())) {
            return usersRoleAssignments.stream().toList();
        }

        RestructuredAccessTypes prmRestructuredAccessTypes =
                convertInRestructuredAccessTypes(accessTypes.getAccessTypes());

        Set<OrganisationProfile> organisationProfiles = prmRestructuredAccessTypes.getOrganisationProfiles();

        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(userRefreshQueue.getAccessTypes());

        //Step 4 Filter PRM access_types to contain only data for the organisation profiles in
        // user_refresh_queue.organisation_profile_ids.

        Set<OrganisationProfile> filteredOrganisationProfiles = 
                getFilteredOrganisationProfiles(userRefreshQueue, organisationProfiles);

        List<UserAccessType> filteredUserAccessTypes =
                getFilteredUserAccessTypes(filteredOrganisationProfiles, userAccessTypes);

        filteredOrganisationProfiles =
                extractOrganisationProfiles(filteredOrganisationProfiles,filteredUserAccessTypes);

        usersRoleAssignments = createRoleAssignments(userRefreshQueue, filteredOrganisationProfiles);

        return usersRoleAssignments.stream().toList();
    }

    private Set<RoleAssignment> createRoleAssignments(UserRefreshQueueEntity userRefreshQueue,
                                                      Set<OrganisationProfile> organisationProfiles) {
        Set<RoleAssignment> usersRoleAssignments = new HashSet<>();
        for (OrganisationProfile organisationProfile:organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictions =
                    organisationProfile.getJurisdictions();
            for (OrganisationProfileJurisdiction orgProfileJurisdictions : organisationProfileJurisdictions) {
                String jurisdictionId = orgProfileJurisdictions.getJurisdictionId();
                for (OrganisationProfileAccessType organisationProfileAccessType:
                        orgProfileJurisdictions.getAccessTypes()) {
                    for (AccessTypeRole accessTypeRole: organisationProfileAccessType.getRoles()) {
                        String organisationalRoleName = accessTypeRole.getOrganisationalRoleName();
                        RoleAssignment roleAssignment =
                                createRoleAssignment(organisationalRoleName,userRefreshQueue.getUserId(),jurisdictionId,
                                        accessTypeRole.getCaseTypeId(),null);
                        usersRoleAssignments.add(roleAssignment);
                        if (accessTypeRole.isGroupAccessEnabled()
                                && StringUtils.isNotBlank(accessTypeRole.getGroupRoleName())
                                && StringUtils.isNotBlank(accessTypeRole.getCaseGroupIdTemplate())) {
                            String roleName = accessTypeRole.getGroupRoleName();
                            String caseAccessGroupId =
                                    generateCaseAccessGroupId(accessTypeRole.getCaseGroupIdTemplate(),
                                            userRefreshQueue.getOrganisationId());
                            RoleAssignment groupRoleAssignment =
                                    createRoleAssignment(roleName,userRefreshQueue.getUserId(),jurisdictionId,
                                            accessTypeRole.getCaseTypeId(),caseAccessGroupId);
                            usersRoleAssignments.add(groupRoleAssignment);
                        }
                    }
                }
            }
        }
        return usersRoleAssignments;
    }

    private Set<OrganisationProfile> extractOrganisationProfiles(Set<OrganisationProfile> organisationProfiles,
                                                                 List<UserAccessType> userAccessTypes) {
        Set<OrganisationProfile> extractedOrganisationProfiles = new HashSet<>();
        for (OrganisationProfile organisationProfile:organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictions =
                    organisationProfile.getJurisdictions();
            for (OrganisationProfileJurisdiction orgProfileJurisdictions : organisationProfileJurisdictions) {
                for (OrganisationProfileAccessType organisationProfileAccessType:
                        orgProfileJurisdictions.getAccessTypes()) {
                    for (UserAccessType userAccessType:userAccessTypes) {
                        if (organisationProfileAccessType.isAccessMandatory()
                                || (organisationProfileAccessType.isAccessDefault() && (userAccessType == null))
                                || userAccessType.getEnabled()) {
                            extractedOrganisationProfiles.add(organisationProfile);
                        }
                    }
                }
            }
        }
        return extractedOrganisationProfiles;
    }

    private static List<UserAccessType> getFilteredUserAccessTypes(Set<OrganisationProfile> organisationProfiles,
                                                                   List<UserAccessType> userAccessTypes) {
        List<UserAccessType> filteredUserAccessTypes = new ArrayList<>();
        for (OrganisationProfile organisationProfile: organisationProfiles) {
            String organisationProfileId = organisationProfile.getOrganisationProfileId();
            for (OrganisationProfileJurisdiction organisationProfileJurisdiction :
                    organisationProfile.getJurisdictions()) {
                String jurisdictionId = organisationProfileJurisdiction.getJurisdictionId();
                for (OrganisationProfileAccessType organisationProfileAccessType :
                        organisationProfileJurisdiction.getAccessTypes()) {
                    String accessTypeID = organisationProfileAccessType.getAccessTypeId();
                    for (UserAccessType userAccessType : userAccessTypes) {
                        if (accessTypeID.equals(userAccessType.getAccessTypeId())
                                && jurisdictionId.equals(userAccessType.getJurisdictionId())
                                && organisationProfileId.equals(userAccessType.getOrganisationProfileId())) {
                            filteredUserAccessTypes.add(userAccessType);
                        }
                    }
                }
            }
        }
        return filteredUserAccessTypes;
    }

    @NotNull
    private static Set<OrganisationProfile> getFilteredOrganisationProfiles(UserRefreshQueueEntity userRefreshQueue,
                                                                    Set<OrganisationProfile> organisationProfiles) {
        String[] ids = userRefreshQueue.getOrganisationProfileIds();
        List<String> idsList = Arrays.stream(ids).toList();
        return organisationProfiles
                .stream()
                .filter(organisationProfile ->
                    idsList.contains(organisationProfile.getOrganisationProfileId())
                ).collect(Collectors.toSet());
    }

    private RoleAssignment createRoleAssignment(String roleName, String userId, String jurisdictionId,
                                                String caseTypeId,String caseAccessGroupId) {

        Map<String, String> attributes = new HashMap<>();

        attributes.put("jurisdiction",jurisdictionId);
        attributes.put("caseType",caseTypeId);
        if (null != caseAccessGroupId) {
            attributes.put("caseAccessGroupId",caseAccessGroupId);
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
                //.beginTime("")//EMPTY
                //.endTime()//EMPTY
                //.notes("")//empty
                .attributes(convertValue(attributes))
                .build();
    }

    private String generateCaseAccessGroupId(String caseGroupIdTemplate, String organisationId) {
        return caseGroupIdTemplate.replace("$ORGID$",organisationId);
    }

    private boolean isOrganisationStatusActive(String organisationStatus) {
        //TODO logic to find the status
        return Arrays.stream(ORGANISATION_ACTIVE_STATUSES).anyMatch(organisationStatus::equals);
    }
}
