package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class RequestMappingService {


    public static final String STAFF_ORGANISATIONAL_ROLE_MAPPING = "staff-organisational-role-mapping";
    public static final String AM_ORG_ROLE_MAPPING_SERVICE = "am_org_role_mapping_service";
    public static final String ROLE_ASSIGNMENTS_QUERY_NAME = "getRoleAssignments";
    public static final String ROLE_ASSIGNMENTS_RESULTS_KEY = "roleAssignments";
    private RoleAssignmentService roleAssignmentService;
    private StatelessKieSession kieSession;
    private SecurityUtils securityUtils;


    /**
     * For each caseworker represented in the map, determine what the role assignments should be,
     * and update them in the role assignment service.
     */
    public ResponseEntity<Object> createCaseWorkerAssignments(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {
        // Get the role assignments for each caseworker in the input profiles.
        Map<String, List<RoleAssignment>> usersRoleAssignments = getCaseworkerRoleAssignments(usersAccessProfiles);
        // The response body is a list of ....???....
        return updateCaseworkersRoleAssignments(usersRoleAssignments);

    }

    /**
     * Apply the role assignment mapping rules to determine what the role assignments should be
     * for each caseworker represented in the map.
     */
    private Map<String, List<RoleAssignment>> getCaseworkerRoleAssignments(Map<String,
            Set<UserAccessProfile>> usersAccessProfiles) {
        // Create a map to hold the role assignments for each user.
        Map<String, List<RoleAssignment>> usersRoleAssignments = new HashMap<>();
        // Make sure every user in the input collection has a list in the map.  This includes users
        // who have been deleted, for whom no role assignments will be created by the rules.
        usersAccessProfiles.keySet().forEach(k -> usersRoleAssignments.put(k, new ArrayList<RoleAssignment>()));
        // Get all the role assignments the rules create for the set of access profiles.
        List<RoleAssignment> roleAssignments = mapUserAccessProfiles(usersAccessProfiles);
        // Add each role assignment to the results map.
        roleAssignments.forEach(ra -> usersRoleAssignments.get(ra.getActorId()).add(ra));
        return usersRoleAssignments;
    }

    /**
     * Run the mapping rules to generate all the role assignments each caseworker represented in the map.
     */
    private List<RoleAssignment> mapUserAccessProfiles(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {

        // Combine all the user profiles into a single collection for the rules engine.
        Set<UserAccessProfile> allProfiles = new HashSet<>();
        usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

        // Sequence of processing for executing the rules:
        //   1. add all the profiles
        //   2. fire all the rules
        //   3. retrieve all the created role assignments
        //      (into a variable populated by the results of a query defined in the rules).
        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsertElements(allProfiles));
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

    /**
     * Update the role assignments for every caseworker represented in the map.
     * Note that some caseworker IDs may have empty role assignment collections.
     * This is OK - these caseworkers have been deleted (or just don't have any appointments which map to roles).
     */
    ResponseEntity<Object> updateCaseworkersRoleAssignments(Map<String, List<RoleAssignment>> usersRoleAssignments) {
        //prepare an empty list of responses
        List<Object> finalResponse = new ArrayList<>();

        usersRoleAssignments.entrySet().stream()
                .forEach(entry -> finalResponse.add(updateCaseworkerRoleAssignments(entry.getKey(),
                        entry.getValue()).getBody()));
        return ResponseEntity.status(HttpStatus.OK).body(finalResponse);
    }

    /**
     * Update a single caseworker's role assignments, using the staff organisational role mapping process ID
     * and the user's ID as the process and reference values.
     */
    ResponseEntity<Object> updateCaseworkerRoleAssignments(String userId, Collection<RoleAssignment> roleAssignments) {
        String process = STAFF_ORGANISATIONAL_ROLE_MAPPING;
        String reference = userId;
        return updateRoleAssignments(process, reference, roleAssignments);
    }

    /**
     * Send an update of role assignments to the role assignment service for a process/reference pair.
     */
    ResponseEntity<Object> updateRoleAssignments(String process, String reference,
                                                 Collection<RoleAssignment> roleAssignments) {
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
        return roleAssignmentService.createRoleAssignment(assignmentRequest);
    }

}
