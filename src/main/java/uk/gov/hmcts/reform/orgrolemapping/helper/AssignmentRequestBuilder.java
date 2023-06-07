package uk.gov.hmcts.reform.orgrolemapping.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

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
    public static final String EMPLOYMENT_TICKET_DESCRIPTION_ENGLAND = "Others - Employment Tribunal England & Wales";
    public static final String EMPLOYMENT_TICKET_DESCRIPTION_SCOTLAND = "Others - Employment Tribunal (Scotland)";

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

    public static Set<UserAccessProfile> convertProfileToJudicialAccessProfile(JudicialProfile judicialProfile) {
        Set<UserAccessProfile> judicialAccessProfiles = new HashSet<>();
        Set<String> ticketCodes = new HashSet<>();
        if (judicialProfile.getAuthorisations() != null) {
            judicialProfile.getAuthorisations().forEach(authorisation -> {
                if (authorisation.getTicketCode() != null && (authorisation.getEndDate() == null
                        || authorisation.getEndDate().compareTo(LocalDateTime.now()) >= 0)) {
                    ticketCodes.add(authorisation.getTicketCode());
                }
                }
            );
        }
        judicialProfile.getAppointments().forEach(appointment -> {
            var judicialAccessProfile = JudicialAccessProfile.builder().build();
            judicialAccessProfile.setUserId(judicialProfile.getSidamId());
            judicialAccessProfile.setRoles(appointment.getRoles());
            judicialAccessProfile.setBeginTime(appointment.getStartDate() == null ? null :
                        appointment.getStartDate().atStartOfDay(ZoneId.of("UTC")));
            judicialAccessProfile.setEndTime(appointment.getEndDate() != null ? appointment.getEndDate()
                    .atStartOfDay(ZoneId.of("UTC")) : null);
            judicialAccessProfile.setRegionId(appointment.getLocationId());
            // change from epimmsid to base location as part of SSCS
            judicialAccessProfile.setBaseLocationId(appointment.getBaseLocationId());
            judicialAccessProfile.setTicketCodes(List.copyOf(ticketCodes));
            judicialAccessProfile.setAppointment(appointment.getAppointment());
            judicialAccessProfile.setAppointmentType(appointment.getAppointmentType());
            judicialAccessProfile.setAuthorisations(judicialProfile.getAuthorisations());
            judicialAccessProfile.setServiceCode(appointment.getServiceCode());
            judicialAccessProfile.setPrimaryLocationId("true"
                    .equalsIgnoreCase(appointment.getIsPrincipalAppointment()) ? appointment.getEpimmsId() : "");
            judicialAccessProfiles.add(judicialAccessProfile);
        });
        return judicialAccessProfiles;
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

    public static boolean validateAuthorisationEmploymentTicketDescription(List<Authorisation> authorisations, String serviceCode) {

        if (!CollectionUtils.isEmpty(authorisations)) {
            return authorisations.stream().anyMatch(authorisation ->
                (authorisation.getServiceCodes() != null && authorisation.getServiceCodes().contains(serviceCode)
                    || (authorisation.getTicketDescription() != null
                        && (authorisation.getTicketDescription().equals(EMPLOYMENT_TICKET_DESCRIPTION_ENGLAND)
                        ||  authorisation.getTicketDescription().equals(EMPLOYMENT_TICKET_DESCRIPTION_SCOTLAND))))
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
}