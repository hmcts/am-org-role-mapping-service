package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AccessTypeRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.PROFESSIONAL_ORGANISATIONAL_ROLE_MAPPING;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertValue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.getRestructuredAccessTypes;

@Service
@Slf4j
@AllArgsConstructor
public class ProfessionalRefreshOrchestrationHelper {

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private final AccessTypesRepository accessTypesRepository;

    private final ObjectMapper objectMapper;

    private final RoleAssignmentService roleAssignmentService;

    private final SecurityUtils securityUtils;
    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";

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
                prdUser.getOrganisationInfo().getStatus(),
                String.join(",", prdUser.getOrganisationInfo().getOrganisationProfileIds())
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
        if (userRefreshQueue.getAccessTypesMinVersion() > accessTypes.getVersion()) {
            return;
        }
        AssignmentRequest assignmentRequest =
                createAssignmentRequest(userRefreshQueue, accessTypes);
        ResponseEntity<Object> responseEntity = roleAssignmentService.createRoleAssignment(assignmentRequest);
        log.info("generateRoleAssignments responseEntity" + responseEntity);
    }


    private AssignmentRequest createAssignmentRequest(UserRefreshQueueEntity userRefreshQueue,
                                                      AccessTypesEntity accessTypes) {

        String reference = userRefreshQueue.getUserId();
        List<RoleAssignment> usersRoleAssignments;
        try {
            usersRoleAssignments = prepareRoleAssignments(userRefreshQueue, accessTypes);
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
        if (null != userRefreshQueue.getDeleted()) {
            return Collections.emptyList();
        }

        if (!isOrganisationStatusActive(userRefreshQueue.getOrganisationStatus())) {
            return Collections.emptyList();
        }

        RestructuredAccessTypes prmRestructuredAccessTypes =
                getRestructuredAccessTypes(accessTypes.getAccessTypes());

        Set<OrganisationProfile> organisationProfiles = prmRestructuredAccessTypes.getOrganisationProfiles();

        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(userRefreshQueue.getAccessTypes());

        Set<OrganisationProfile> filteredOrganisationProfiles =
                getFilteredOrganisationProfiles(userRefreshQueue, organisationProfiles);

        filteredOrganisationProfiles =
                getFilteredOrgProfilesUserAccessTypes(filteredOrganisationProfiles, userAccessTypes);

        filteredOrganisationProfiles = extractOrganisationProfiles(filteredOrganisationProfiles, userAccessTypes);

        return createRoleAssignments(userRefreshQueue, filteredOrganisationProfiles).stream().toList();
    }

    private Set<RoleAssignment> createRoleAssignments(UserRefreshQueueEntity userRefreshQueue,
                                                      Set<OrganisationProfile> organisationProfiles) {
        Set<RoleAssignment> usersRoleAssignments = new HashSet<>();
        for (OrganisationProfile organisationProfile : organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictions =
                    organisationProfile.getJurisdictions();
            for (OrganisationProfileJurisdiction orgProfileJurisdictions : organisationProfileJurisdictions) {
                String jurisdictionId = orgProfileJurisdictions.getJurisdictionId();
                for (OrganisationProfileAccessType organisationProfileAccessType :
                        orgProfileJurisdictions.getAccessTypes()) {
                    for (AccessTypeRole accessTypeRole : organisationProfileAccessType.getRoles()) {
                        String organisationalRoleName = accessTypeRole.getOrganisationalRoleName();
                        if (StringUtils.isNotBlank(organisationalRoleName)) {
                            RoleAssignment roleAssignment =
                                    createRoleAssignment(organisationalRoleName, userRefreshQueue.getUserId(),
                                            jurisdictionId, accessTypeRole.getCaseTypeId(), null);
                            usersRoleAssignments.add(roleAssignment);
                        }
                        if (accessTypeRole.isGroupAccessEnabled()
                                && StringUtils.isNotBlank(accessTypeRole.getGroupRoleName())
                                && StringUtils.isNotBlank(accessTypeRole.getCaseGroupIdTemplate())) {
                            String roleName = accessTypeRole.getGroupRoleName();
                            String caseAccessGroupId =
                                    generateCaseAccessGroupId(accessTypeRole.getCaseGroupIdTemplate(),
                                            userRefreshQueue.getOrganisationId());
                            RoleAssignment groupRoleAssignment =
                                    createRoleAssignment(roleName, userRefreshQueue.getUserId(), jurisdictionId,
                                            accessTypeRole.getCaseTypeId(), caseAccessGroupId);
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
        for (OrganisationProfile organisationProfile : organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictions =
                    organisationProfile.getJurisdictions();
            for (OrganisationProfileJurisdiction orgProfileJurisdictions : organisationProfileJurisdictions) {
                for (OrganisationProfileAccessType organisationProfileAccessType :
                        orgProfileJurisdictions.getAccessTypes()) {
                    for (UserAccessType userAccessType : userAccessTypes) {
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

    private static Set<OrganisationProfile> getFilteredOrgProfilesUserAccessTypes(
            Set<OrganisationProfile> organisationProfiles, List<UserAccessType> userAccessTypes) {

        Set<OrganisationProfile> filteredOrganisationProfiles = new HashSet<>();
        for (OrganisationProfile organisationProfile : organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictionSet;
            organisationProfileJurisdictionSet =
                    getMatchingOrganisationProfileJurisdiction(organisationProfile, userAccessTypes);
            organisationProfile.getJurisdictions().clear();
            organisationProfile.setJurisdictions(organisationProfileJurisdictionSet);
            filteredOrganisationProfiles.add(organisationProfile);
        }
        return filteredOrganisationProfiles;
    }

    private static Set<OrganisationProfileJurisdiction> getMatchingOrganisationProfileJurisdiction(
            OrganisationProfile organisationProfile, List<UserAccessType> userAccessTypes) {
        Set<OrganisationProfileJurisdiction> matchingResults = new HashSet<>();
        String organisationProfileId = organisationProfile.getOrganisationProfileId();
        if (null != organisationProfileId) {
            for (UserAccessType userAccessType : userAccessTypes) {
                matchByOrganisationProfileId(matchingResults, organisationProfile, userAccessType);
            }
        }
        return matchingResults;
    }

    private static void matchByOrganisationProfileId(
            Set<OrganisationProfileJurisdiction> matchingResults,
            OrganisationProfile organisationProfile, UserAccessType userAccessType) {
        if (organisationProfile.getOrganisationProfileId().equals(userAccessType.getOrganisationProfileId())) {
            for (OrganisationProfileJurisdiction opj : organisationProfile.getJurisdictions()) {
                matchByOrganisationJurisdiction(matchingResults, opj, userAccessType);
            }
        }
    }

    private static void matchByOrganisationJurisdiction(
            Set<OrganisationProfileJurisdiction> matchingResults,
            OrganisationProfileJurisdiction opj, UserAccessType userAccessType) {
        String jurisdictionId = opj.getJurisdictionId();
        if (null != jurisdictionId && (jurisdictionId.equals(userAccessType.getJurisdictionId()))) {
            for (OrganisationProfileAccessType opat : opj.getAccessTypes()) {
                matchByAccessType(matchingResults, opat, userAccessType, opj);
            }
        }
    }

    private static void matchByAccessType(
            Set<OrganisationProfileJurisdiction> matchingResults,
            OrganisationProfileAccessType opat, UserAccessType userAccessType, OrganisationProfileJurisdiction opj) {
        String accessTypeID = opat.getAccessTypeId();
        if (accessTypeID != null && accessTypeID.equals(userAccessType.getAccessTypeId())) {
            matchingResults.add(opj);
        }
    }

    @NotNull
    private static Set<OrganisationProfile> getFilteredOrganisationProfiles(
            UserRefreshQueueEntity userRefreshQueue, Set<OrganisationProfile> organisationProfiles) {
        String[] ids = userRefreshQueue.getOrganisationProfileIds();
        List<String> idsList = Arrays.stream(ids).toList();
        return organisationProfiles
                .stream()
                .filter(organisationProfile ->
                        idsList.contains(organisationProfile.getOrganisationProfileId())
                ).collect(Collectors.toSet());
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
