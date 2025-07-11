package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileAccessType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfileJurisdiction;
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
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.PROFESSIONAL_ORGANISATIONAL_ROLE_MAPPING;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInRestructuredAccessTypes;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertValue;

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
                prdUser.getOrganisationInfo().getStatus().name(),
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

        // Process any further record(s) recursively
        //processActiveUserRefreshQueue(accessTypes);
    }

    public void refreshSingleUser(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        log.debug("Refreshing professional role assignments for user '{}'", userRefreshQueue.getUserId());
        generateRoleAssignments(userRefreshQueue, accessTypes);
        userRefreshQueue.setActive(false);
    }

    private void generateRoleAssignments(UserRefreshQueueEntity userRefreshQueue, AccessTypesEntity accessTypes) {
        //Step 1 If user_refresh_queue.access_types_min_version > PRM access_types.version,
        //        THEN abort processing for this user and do not clear the user_refresh_queue record.
        //NoteThis will be swept up in a later run.  An alternative would be to re-retrieve the access types data from
        // the PRM database, since it must exist there at a usable version.
        if (userRefreshQueue.getAccessTypesMinVersion() > accessTypes.getVersion().intValue()) {
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
        // Step 2 If user_refresh_queue.deleted != null then halt and return an empty set of role assignments.
        if (null != userRefreshQueue.getDeleted()) {
            return Collections.emptyList();
        }

        // Step 3 If user_refresh_queue.organisation_status is not an active status, then halt and return an empty set
        // of role assignments. Note The set of active statuses will have to be provided by PRD team / product owner.

        if (!isOrganisationStatusActive(userRefreshQueue.getOrganisationStatus())) {
            return Collections.emptyList();
        }

        RestructuredAccessTypes prmRestructuredAccessTypes =
                convertInRestructuredAccessTypes(accessTypes.getAccessTypes());

        Set<OrganisationProfile> organisationProfiles = prmRestructuredAccessTypes.getOrganisationProfiles();

        List<UserAccessType> userAccessTypes = JacksonUtils.convertUserAccessTypes(userRefreshQueue.getAccessTypes());

        //Step 4 Filter PRM access_types to contain only data for the organisation profiles in
        // user_refresh_queue.organisation_profile_ids.

        Set<OrganisationProfile> filteredOrganisationProfiles =
                getFilteredOrganisationProfiles(userRefreshQueue, organisationProfiles);

        filteredOrganisationProfiles =
                getFilteredOrgProfilesUserAccessTypes(filteredOrganisationProfiles, userAccessTypes);

        filteredOrganisationProfiles = extractOrganisationProfiles(filteredOrganisationProfiles,userAccessTypes);

        return createRoleAssignments(userRefreshQueue, filteredOrganisationProfiles).stream().toList();
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
                    if (isNullOrEmpty(userAccessTypes)) {
                        extractedOrganisationProfiles.add(organisationProfile);
                    } else {
                        for (UserAccessType userAccessType : userAccessTypes) {
                            if (organisationProfileAccessType.isAccessMandatory()
                                    || (organisationProfileAccessType.isAccessDefault() && (userAccessType == null))
                                    || Boolean.TRUE.equals(userAccessType.getEnabled())) {
                                extractedOrganisationProfiles.add(organisationProfile);
                            }
                        }
                    }
                }
            }
        }
        return extractedOrganisationProfiles;
    }

    private static Set<OrganisationProfile> getFilteredOrgProfilesUserAccessTypes(
            Set<OrganisationProfile> organisationProfiles,List<UserAccessType> userAccessTypes) {

        Set<OrganisationProfile> filteredOrganisationProfiles = new HashSet<>();
        for (OrganisationProfile organisationProfile : organisationProfiles) {
            Set<OrganisationProfileJurisdiction> organisationProfileJurisdictionSet;

            organisationProfileJurisdictionSet =
                    getMatchingOrganisationProfileJurisdiction(organisationProfile,userAccessTypes);
            organisationProfile.getJurisdictions().clear();
            organisationProfile.setJurisdictions(organisationProfileJurisdictionSet);

            filteredOrganisationProfiles.add(organisationProfile);
        }
        return filteredOrganisationProfiles;
    }

    private static Set<OrganisationProfileJurisdiction> getMatchingOrganisationProfileJurisdiction(
            OrganisationProfile organisationProfile, List<UserAccessType> userAccessTypes) {
        Set<OrganisationProfileJurisdiction> matchingOrganisationProfileJurisdiction = new HashSet<>();
        String organisationProfileId = organisationProfile.getOrganisationProfileId();
        if (null != organisationProfileId) {
            if (isNullOrEmpty(userAccessTypes)) {
                for (OrganisationProfileJurisdiction opj : organisationProfile.getJurisdictions()) {
                    Set<OrganisationProfileAccessType> opatSet =
                            getMatchingOpatForNullUserAccessType(opj);
                    if (!opatSet.isEmpty()) {
                        opj.getAccessTypes().clear();
                        opj.setAccessTypes(opatSet);
                        matchingOrganisationProfileJurisdiction.add(opj);
                    }
                }
            } else {
                for (UserAccessType userAccessType : userAccessTypes) {
                    if (organisationProfileId.equals(userAccessType.getOrganisationProfileId())) {
                        for (OrganisationProfileJurisdiction opj : organisationProfile.getJurisdictions()) {
                            String jurisdictionId =  opj.getJurisdictionId();
                            if (null != jurisdictionId && (jurisdictionId.equals(userAccessType.getJurisdictionId()))) {
                                Set<OrganisationProfileAccessType> opatSet =
                                        getMatchingOPAT(opj,userAccessType.getAccessTypeId());
                                if (!opatSet.isEmpty()) {
                                    opj.getAccessTypes().clear();
                                    opj.setAccessTypes(opatSet);
                                    matchingOrganisationProfileJurisdiction.add(opj);
                                }
                            }
                        }
                    }
                }
            }
        }
        return matchingOrganisationProfileJurisdiction;
    }

    public static boolean isNullOrEmpty(final Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static Set<OrganisationProfileAccessType> getMatchingOpatForNullUserAccessType(
            OrganisationProfileJurisdiction opj) {

        return opj.getAccessTypes()
                .stream()
                .filter(ProfessionalRefreshOrchestrationHelper::matchOpatForNullUserAccessType)
                .collect(Collectors.toSet());
    }

    private static boolean matchOpatForNullUserAccessType(OrganisationProfileAccessType opat) {
        return opat.isAccessDefault();
    }

    private static boolean matchOPAT(OrganisationProfileAccessType opat, String userAccessTypeId) {
        return opat.getAccessTypeId() != null && opat.getAccessTypeId().equals(userAccessTypeId);
    }

    private static Set<OrganisationProfileAccessType> getMatchingOPAT(
            OrganisationProfileJurisdiction opj, String userAccessTypeId) {

        return opj.getAccessTypes()
                .stream()
                .filter(organisationProfileAccessType -> matchOPAT(organisationProfileAccessType,userAccessTypeId))
                .collect(Collectors.toSet());
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
        return OrganisationStatus.valueOf(organisationStatus).isActive();
    }
}
