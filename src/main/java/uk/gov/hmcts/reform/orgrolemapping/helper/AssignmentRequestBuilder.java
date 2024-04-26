package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AuthorisationV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.reform.orgrolemapping.util.UtilityFunctions.getAppointmentTypeFromAppointment;
import static uk.gov.hmcts.reform.orgrolemapping.util.UtilityFunctions.localDateToLocalDateTime;
import static uk.gov.hmcts.reform.orgrolemapping.util.UtilityFunctions.localDateToZonedDateTime;
import static uk.gov.hmcts.reform.orgrolemapping.util.UtilityFunctions.stringListToDistinctList;

@Setter
@Slf4j
public class AssignmentRequestBuilder {

    public static final String ASSIGNER_ID = "123e4567-e89b-42d3-a456-556642445678";
    public static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    public static final String PROCESS_ID = "staff-organisational-role-mapping";
    public static final String JUDICIAL_PROCESS_ID = "judicial-organisational-role-mapping";
    public static final String ROLE_NAME_TCW = "tribunal-caseworker";
    public static final String ROLE_NAME_SJ = "judge";
    public static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";

    private AssignmentRequestBuilder() {
        //not meant to be instantiated.
    }

    public static AssignmentRequest buildAssignmentRequest(Boolean replaceExisting) {
        return new AssignmentRequest(buildRequest(replaceExisting),
                buildRequestedRoleCollection());
    }

    public static AssignmentRequest buildJudicialAssignmentRequest(Boolean replaceExisting) {
        return new AssignmentRequest(buildJudicialRequest(replaceExisting),
                buildJudicialRequestedRoleCollection());
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
        try (var inputStream =
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

    public static Set<UserAccessProfile> convertUserProfileToCaseworkerAccessProfile(CaseWorkerProfile
                                                                                             caseWorkerProfile) {
        var startTime = System.currentTimeMillis();
        //roleId X serviceCode
        Set<UserAccessProfile> caseWorkerAccessProfiles = new HashSet<>();

        caseWorkerProfile.getRole().forEach(role ->
                caseWorkerProfile.getWorkArea().forEach(workArea -> {
                    var caseWorkerAccessProfile = new CaseWorkerAccessProfile();
                    caseWorkerAccessProfile.setId(caseWorkerProfile.getId());
                    caseWorkerAccessProfile.setSuspended(caseWorkerProfile.isSuspended());
                    caseWorkerProfile.getBaseLocation().forEach(baseLocation -> {
                        if (baseLocation.isPrimary()) {
                            caseWorkerAccessProfile.setPrimaryLocationId(baseLocation.getLocationId());
                            caseWorkerAccessProfile.setPrimaryLocationName(baseLocation.getLocation());
                        }
                    });
                    caseWorkerAccessProfile.setAreaOfWorkId(workArea.getAreaOfWork());
                    caseWorkerAccessProfile.setServiceCode(workArea.getServiceCode());
                    caseWorkerAccessProfile.setRoleId(role.getRoleId());
                    caseWorkerAccessProfile.setRoleName(role.getRoleName());
                    caseWorkerAccessProfile.setCaseAllocatorFlag(caseWorkerProfile.getCaseAllocator());
                    caseWorkerAccessProfile.setTaskSupervisorFlag(caseWorkerProfile.getTaskSupervisor());
                    caseWorkerAccessProfile.setRegionId(Long.toString(caseWorkerProfile.getRegionId()));
                    caseWorkerAccessProfile.setStaffAdmin(caseWorkerProfile.getStaffAdmin());
                    if (caseWorkerProfile.getSkills() != null) {
                        caseWorkerAccessProfile.setSkillCodes(caseWorkerProfile.getSkills().stream()
                            .map(CaseWorkerProfile.Skills::getSkillCode).toList());

                    }
                    caseWorkerAccessProfiles.add(caseWorkerAccessProfile);

                })
        );

        log.debug("Execution time of convertUserProfileToUserAccessProfile() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));

        log.debug("Execution time of convertUserProfileToUserAccessProfile() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));
        return caseWorkerAccessProfiles;
    }

    @SuppressWarnings("deprecation")
    public static Set<UserAccessProfile> convertProfileToJudicialAccessProfileV2(JudicialProfileV2 judicialProfile) {
        Set<UserAccessProfile> judicialAccessProfiles = new HashSet<>();

        List<String> roles = getActiveRoles(judicialProfile.getRoles()).stream()
            .map(RoleV2::getJurisdictionRoleName)
            .distinct()
            .toList();

        // flatten for each appointment
        judicialProfile.getAppointments().forEach(appointment -> {
            var associatedAuthorisations = getV1Authorisations(
                    getAuthorisationsByAppointmentId(judicialProfile.getAuthorisations(),
                            appointment.getAppointmentId())
            );

            var ticketCodes = getActiveTicketCodes(associatedAuthorisations);

            var serviceCodes = stringListToDistinctList(appointment.getServiceCodes());
            // NB: need at least 1 service code: so add null if missing
            if (serviceCodes.isEmpty()) {
                serviceCodes.add(null);
            }

            // flatten for each appointment.serviceCode
            serviceCodes.forEach(serviceCode -> {
                var judicialAccessProfile = JudicialAccessProfile.builder()
                    .userId(judicialProfile.getSidamId())
                    .roles(roles)
                    .beginTime(localDateToZonedDateTime(appointment.getStartDate()))
                    .endTime(localDateToZonedDateTime(appointment.getEndDate()))
                    .regionId(appointment.getCftRegionID())
                    .baseLocationId(appointment.getBaseLocationId())
                    .ticketCodes(stringListToDistinctList(ticketCodes))
                    .appointment(appointment.getAppointment())
                    .contractTypeId(appointment.getContractTypeId())
                    .appointmentType(getAppointmentTypeFromAppointment(appointment))
                    .authorisations(associatedAuthorisations)
                    .serviceCode(serviceCode)
                    .primaryLocationId("true".equalsIgnoreCase(
                            appointment.getIsPrincipalAppointment()) ? appointment.getEpimmsId() : ""
                    )
                    .build();
                judicialAccessProfiles.add(judicialAccessProfile);
            });
        });

        return judicialAccessProfiles;
    }

    private static List<RoleV2> getActiveRoles(List<RoleV2> roles) {
        return roles != null
            ? roles.stream()
                .filter(role -> (role.getJurisdictionRoleName() != null)
                    // AND end date not set or is valid
                    && (role.getEndDate() == null || !role.getEndDate().isBefore(LocalDate.now()))
                )
                .toList()
            : new ArrayList<>();
    }

    private static List<String> getActiveTicketCodes(List<Authorisation> authorisations) {
        return authorisations.stream()
            .filter(authorisation -> (authorisation.getTicketCode() != null)
                // AND end date not set or is valid
                && (authorisation.getEndDate() == null || !authorisation.getEndDate().isBefore(LocalDateTime.now()))
            )
            .map(Authorisation::getTicketCode)
            .toList();
    }

    private static List<AuthorisationV2> getAuthorisationsByAppointmentId(List<AuthorisationV2> authorisations,
                                                                          String appointmentId) {
        return authorisations != null
            ? authorisations.stream()
                .filter(authorisation ->
                    // authorisation either not mapped to any appointment or matched to current appointment
                    (authorisation.getAppointmentId() == null || authorisation.getAppointmentId().equals(appointmentId))
                )
                .toList()
            : new ArrayList<>();
    }

    private static List<Authorisation> getV1Authorisations(List<AuthorisationV2> authorisations) {
        return authorisations != null
            ? authorisations.stream()
                // convert to legacy format
                .map(AssignmentRequestBuilder::authorisationV2ToV1)
                .toList()
            : new ArrayList<>();
    }

    private static Authorisation authorisationV2ToV1(AuthorisationV2 authorisationV2) {
        return Authorisation.builder()
            .ticketCode(authorisationV2.getTicketCode())
            .ticketDescription(authorisationV2.getTicketDescription())
            .jurisdiction(authorisationV2.getJurisdiction())
            .startDate(localDateToLocalDateTime(authorisationV2.getStartDate()))
            .endDate(localDateToLocalDateTime(authorisationV2.getEndDate()))
            .serviceCodes(stringListToDistinctList(authorisationV2.getServiceCodes()))
            .build();
    }


    public static Request buildJudicialRequest(Boolean replaceExisting) {

        return Request.builder()
                .assignerId(ASSIGNER_ID) // This won't be set for the requests.
                .process((JUDICIAL_PROCESS_ID))
                .requestType(RequestType.CREATE)
                .correlationId(UUID.randomUUID().toString())
                .replaceExisting(replaceExisting)
                .build();
    }

    public static Collection<RoleAssignment> buildJudicialRequestedRoleCollection() {
        Collection<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(buildJudicialRoleAssignment());
        return requestedRoles;
    }

    public static boolean validateAuthorisation(List<Authorisation> authorisations, String serviceCode) {

        if (!CollectionUtils.isEmpty(authorisations)) {
            return authorisations.stream().anyMatch(authorisation ->
                    authorisation.getServiceCodes() != null && authorisation.getServiceCodes().contains(serviceCode)
                    && (authorisation.getEndDate() == null
                    || authorisation.getEndDate().compareTo(LocalDateTime.now()) >= 0));

        } else {
            return false;
        }
    }

    public static RoleAssignment buildJudicialRoleAssignment() {
        return RoleAssignment.builder()
                .actorId(ACTOR_ID)
                .actorIdType(ActorIdType.IDAM)
                .roleType(RoleType.ORGANISATION)
                .roleName(ROLE_NAME_SJ)
                .classification(Classification.PUBLIC)
                .grantType(GrantType.STANDARD)
                .roleCategory(RoleCategory.JUDICIAL)
                .readOnly(false)
                .attributes(JacksonUtils.convertValue(buildAttributesFromFile("judicialAttributes.json")))
                .build();
    }


    public static RoleAssignment cloneNewRoleAssignmentAndChangeRegion(RoleAssignment roleAssignment, String region) {
        Map<String,JsonNode> attribute = CollectionUtils.isEmpty(roleAssignment.getAttributes())
                ? new HashMap<>() // default if empty
                : new HashMap<>(roleAssignment.getAttributes()); // clone
        attribute.put("region", JacksonUtils.convertObjectIntoJsonNode(region));

        return RoleAssignment.builder()
                // NB: copy only the properties required for a NEW role assignment
                .actorIdType(roleAssignment.getActorIdType())
                .actorId(roleAssignment.getActorId())
                .roleType(roleAssignment.getRoleType())
                .roleName(roleAssignment.getRoleName())
                .classification(roleAssignment.getClassification())
                .grantType(roleAssignment.getGrantType())
                .roleCategory(roleAssignment.getRoleCategory())
                .readOnly(roleAssignment.isReadOnly())
                .beginTime(roleAssignment.getBeginTime())
                .endTime(roleAssignment.getEndTime())
                .attributes(attribute)
                .notes(roleAssignment.getNotes())
                .authorisations(roleAssignment.getAuthorisations())
                .build();
    }

}
