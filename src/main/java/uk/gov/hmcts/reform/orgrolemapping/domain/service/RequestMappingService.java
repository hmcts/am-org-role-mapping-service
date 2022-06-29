package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.config.DBFlagConfigurtion;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class RequestMappingService<T> {

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Autowired
    private PersistenceService persistenceService;

    public static final String STAFF_ORGANISATIONAL_ROLE_MAPPING = "staff-organisational-role-mapping";
    public static final String JUDICIAL_ORGANISATIONAL_ROLE_MAPPING = "judicial-organisational-role-mapping";
    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";
    public static final String ROLE_ASSIGNMENTS_QUERY_NAME = "getRoleAssignments";
    public static final String ROLE_ASSIGNMENTS_RESULTS_KEY = "roleAssignments";

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    @Autowired
    private StatelessKieSession kieSession;

    @Autowired
    private SecurityUtils securityUtils;


    /**
     * For each caseworker represented in the map, determine what the role assignments should be,
     * and update them in the role assignment service.
     */
    public ResponseEntity<Object> createAssignments(Map<String, Set<T>> usersAccessProfiles,
                                                    List<JudicialBooking> judicialBookings, UserType userType) {
        var startTime = System.currentTimeMillis();
        // Get the role assignments for each caseworker in the input profiles.
        Map<String, List<RoleAssignment>> usersRoleAssignments = getProfileRoleAssignments(usersAccessProfiles,
                judicialBookings, userType);
        // The response body is a list of ....???....
        ResponseEntity<Object> responseEntity = updateProfilesRoleAssignments(usersRoleAssignments, userType);
        log.debug("Execution time of createCaseWorkerAssignments() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));

        return responseEntity;

    }

    public ResponseEntity<Object> createAssignments(Map<String, Set<T>> usersAccessProfiles, UserType userType) {
        return createAssignments(usersAccessProfiles, Collections.emptyList(), userType);
    }

    /**
     * Apply the role assignment mapping rules to determine what the role assignments should be
     * for each user profile represented in the map.
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<RoleAssignment>> getProfileRoleAssignments(Map<String,
            Set<T>> usersAccessProfiles, List<JudicialBooking> judicialBookings, UserType userType) {

        // Create a map to hold the role assignments for each user.
        Map<String, List<RoleAssignment>> usersRoleAssignments = new HashMap<>();

        // Make sure every user in the input collection has a list in the map.  This includes users
        // who have been deleted, for whom no role assignments will be created by the rules.
        usersAccessProfiles.keySet().forEach(k -> usersRoleAssignments.put(k, new ArrayList<>()));
        // Get all the role assignments created for the set of access profiles.
        List<RoleAssignment> roleAssignments = mapUserAccessProfiles(usersAccessProfiles, judicialBookings);
        // Add each role assignment to the results map.
        roleAssignments.forEach(ra -> usersRoleAssignments.get(ra.getActorId()).add(ra));


        // if List<RoleAssignment> is empty in case of suspended false in corresponding
        // user access profile then remove
        // entry of userProfile from usersRoleAssignments map
        List<String> needToRemoveUAP = new ArrayList<>();

        if (userType.equals(UserType.CASEWORKER)) {

            //Identify the user with empty List<RoleAssignment> in case of suspended is false.
            usersRoleAssignments.forEach((k, v) -> {
                if (v.isEmpty()) {
                    Set<CaseWorkerAccessProfile> accessProfiles = (Set<CaseWorkerAccessProfile>) usersAccessProfiles
                            .get(k);
                    if (!requireNonNull(accessProfiles.stream().findFirst().orElse(null)).isSuspended()) {
                        needToRemoveUAP.add(k);
                    }
                }

            });
        } else if (userType.equals(UserType.JUDICIAL)) {
            //Identify the user with empty List<RoleAssignment> in case of suspended is false.
            usersRoleAssignments.forEach((k, v) -> {
                if (v.isEmpty()) {
                    needToRemoveUAP.add(k);
                }
            });

        }

        //remove the entry of user from map in case of empty if suspended is false
        log.info("Count of expired/suspended/rejected access profiles in ORM : {} ", needToRemoveUAP.size());
        log.info("Access profiles for empty request for RAS: {} ", needToRemoveUAP);

        Map<String, Integer> roleAssignmentsCount = new HashMap<>();
        //print usersRoleAssignments
        usersRoleAssignments.forEach((k, v) -> {
            roleAssignmentsCount.put(k, v.size());
            log.debug("UserId {} having the RoleAssignments created by the drool  {}  ", k,
                    v);
        });

        log.info("Count of RoleAssignments corresponding to the UserId ::{}  ", roleAssignmentsCount);


        return usersRoleAssignments;
    }

    /**
     * Run the mapping rules to generate all the role assignments each caseworker represented in the map.
     */
    private List<RoleAssignment> mapUserAccessProfiles(Map<String, Set<T>> usersAccessProfiles,
                                                       List<JudicialBooking> judicialBookings) {
        var startTime = System.currentTimeMillis();
        List<RoleAssignment> roleAssignments = getRoleAssignments(usersAccessProfiles, judicialBookings);
        log.debug("Execution time of mapUserAccessProfiles() in RoleAssignment : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));

        return roleAssignments;
    }

    @NotNull
    List<RoleAssignment> getRoleAssignments(Map<String, Set<T>> usersAccessProfiles,
                                            List<JudicialBooking> judicialBookings) {
        // Combine all the user profiles into a single collection for the rules engine.
        Set<T> allProfiles = new HashSet<>();
        usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

        // Sequence of processing for executing the rules:
        //   1. add all the profiles
        //   2. fire all the rules
        //   3. retrieve all the created role assignments
        //      (into a variable populated by the results of a query defined in the rules).
        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsertElements(allProfiles));
        List<FeatureFlag> featureFlags = getDBFeatureFlags();
        commands.add(CommandFactory.newInsertElements(featureFlags));
        commands.add(CommandFactory.newInsertElements(judicialBookings));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME));

        // Run the rules
        ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));

        // Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }
        return roleAssignments;
    }

    private List<FeatureFlag> getDBFeatureFlags() {
        List<FeatureFlag> featureFlags = new ArrayList<>();
        Map<String, Boolean> droolFlagStates = new ConcurrentHashMap<>();
        // building the LDFeature Flag
        if (environment.equals("prod")) {
            droolFlagStates = DBFlagConfigurtion.getDroolFlagStates();
        } else {
            // fetch the latest value from db for lower env
            getFlagValuesFromDB(droolFlagStates);
        }

        for (Map.Entry<String, Boolean> flag : droolFlagStates.entrySet()) {
            var featureFlag = FeatureFlag.builder()
                    .flagName(flag.getKey())
                    .status(flag.getValue())
                    .build();
            featureFlags.add(featureFlag);
        }
        featureFlags.forEach(a -> log.debug("featureFlag values from db.....{}   ", a.getFlagName()
                + " - " + a.isStatus()));
        return featureFlags;
    }

    /**
     * Update the role assignments for every caseworker represented in the map.
     * Note that some caseworker IDs may have empty role assignment collections.
     * This is OK - these caseworkers have been deleted (or just don't have any appointments which map to roles).
     */
    ResponseEntity<Object> updateProfilesRoleAssignments(Map<String, List<RoleAssignment>> usersRoleAssignments,
                                                         UserType userType) {
        //prepare an empty list of responses
        List<Object> finalResponse = new ArrayList<>();
        AtomicInteger failureResponseCount = new AtomicInteger();

        usersRoleAssignments
                .forEach((k, v) -> finalResponse.add(updateProfileRoleAssignments(k,
                        v, failureResponseCount, userType)));
        log.info("Count of failure responses from RAS : {} ", failureResponseCount.get());
        log.info("Count of Success responses from RAS : {} ", (finalResponse.size() - failureResponseCount.get()));
        return ResponseEntity.status(HttpStatus.OK).body(finalResponse);
    }

    /**
     * Update a single caseworker's role assignments, using the staff organisational role mapping process ID
     * and the user's ID as the process and reference values.
     */
    ResponseEntity<Object> updateProfileRoleAssignments(String userId, Collection<RoleAssignment> roleAssignments,
                                                        AtomicInteger failureResponseCount, UserType userType) {

        ResponseEntity<Object> responseEntity = null;

        // Print response code  of RAS for each userID
        if (userType.equals(UserType.CASEWORKER)) {
            responseEntity = updateRoleAssignments(STAFF_ORGANISATIONAL_ROLE_MAPPING,
                    userId, roleAssignments);
        } else if (userType.equals(UserType.JUDICIAL)) {
            responseEntity = updateRoleAssignments(JUDICIAL_ORGANISATIONAL_ROLE_MAPPING,
                    userId, roleAssignments);
        }
        if (responseEntity != null && responseEntity.getStatusCode() != HttpStatus.CREATED) {
            failureResponseCount.getAndIncrement();
        }

        log.info("Role Assignment Service response status is {} for the userId {}", requireNonNull(responseEntity)
                .getStatusCode(), userId);

        return responseEntity;
    }

    /**
     * Send an update of role assignments to the role assignment service for a process/reference pair.
     */
    ResponseEntity<Object> updateRoleAssignments(String process, String reference,
                                                 Collection<RoleAssignment> roleAssignments) {
        var startTime = System.currentTimeMillis();
        AssignmentRequest assignmentRequest =
                AssignmentRequest.builder()
                        .request(
                                Request.builder()
                                        .requestType(RequestType.CREATE)
                                        .replaceExisting(true)
                                        .process(process)
                                        .reference(reference)
                                        .assignerId(securityUtils.getUserId())
                                        .clientId(AM_ORG_ROLE_MAPPING_SERVICE)
                                        .correlationId(UUID.randomUUID().toString())
                                        .build())
                        .requestedRoles(roleAssignments)
                        .build();
        ResponseEntity<Object> responseEntity;

        try {
            responseEntity = roleAssignmentService.createRoleAssignment(assignmentRequest);
            log.debug("Execution time of updateRoleAssignments() : {} ms",
                    (Math.subtractExact(System.currentTimeMillis(), startTime)));

        } catch (FeignException.FeignClientException feignClientException) {
            log.error("Handling FeignClientException UnprocessableEntity: " + feignClientException.getMessage());

            AssignmentRequest assignmentRequest1 = new AssignmentRequest();
            try {
                assignmentRequest1 = JacksonUtils.readValue(feignClientException.contentUTF8());
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }

            responseEntity = new ResponseEntity<>(
                    new RoleAssignmentRequestResource(assignmentRequest1), HttpStatus.UNPROCESSABLE_ENTITY);

        }
        return responseEntity;
    }

    /**
     * This utility method is used to capture the log in drools.
     */
    public static void logMsg(final String message) {
        log.debug(message);
    }

    /**
     * This utility method is used to capture the log in drools.
     */
    public static void logInfoMsg(final String message) {
        log.info(message);
    }

    public static List<String> addAndGetTicketCodes(List<String> existingTicketCodes, String newTicketCode) {
        List<String> updatedTicketCodes = new ArrayList<>(existingTicketCodes);
        updatedTicketCodes.add(newTicketCode);
        return updatedTicketCodes;
    }

    private void getFlagValuesFromDB(Map<String, Boolean> droolFlagStates) {
        for (FeatureFlagEnum featureFlagEnum : FeatureFlagEnum.values()) {
            var status = persistenceService.getStatusByParam(featureFlagEnum.getValue(), environment);
            droolFlagStates.put(featureFlagEnum.getValue(), status);
        }
    }

}
