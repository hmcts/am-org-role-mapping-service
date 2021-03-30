package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ValidationModelService {

    private StatelessKieSession kieSession;
    public static final String ROLE_ASSIGNMENTS_QUERY_NAME = "getRoleAssignments";
    public static final String ROLE_ASSIGNMENTS_RESULTS_KEY = "roleAssignments";
    private SecurityUtils securityUtils;

    //1. receive single UserAccessProfile for caseworker
    //2. receive initial requestedRole corresponding to above userAccessProfile
    //3. Run the rules for preparing the final requestedRole.

    public List<AssignmentRequest> runRulesOnAccessProfiles(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {

        // Combine all the user profiles into a single collection for the rules engine.
        Set<UserAccessProfile> allProfiles = new HashSet<>();
        usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

        List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsertElements(allProfiles));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME));

        // Run the rules
        ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));

        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults) results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
            roleAssignments.add((RoleAssignment) row.get("$roleAssignment"));
        }

        List<AssignmentRequest> assignmentRequests = new ArrayList<>();
        roleAssignments.forEach(k -> assignmentRequests.add(mapper(k)));

        return assignmentRequests;

    }

    public AssignmentRequest mapper(RoleAssignment roleAssignment) {

        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("caseId", JacksonUtils.convertObjectIntoJsonNode("123456"));
        attributes.put("jurisdiction", JacksonUtils.convertObjectIntoJsonNode("IA"));

        return AssignmentRequest.builder()
                        .request(
                                Request.builder()
                                        .requestType(RequestType.CREATE)
                                        .process("staff-organisational-role-mapping")
                                        .assignerId("a3695334-9c9f-4da0-b073-c9b0b5c68247")
                                        .reference(roleAssignment.getActorId())
                                        .replaceExisting(true)
                                        .correlationId(UUID.randomUUID().toString())
                                        .build())
                        .requestedRoles(Collections.singletonList(
                                RoleAssignment.builder()
                                        .actorId(roleAssignment.getActorId())
                                        .actorIdType(roleAssignment.getActorIdType())
                                        .roleType(roleAssignment.getRoleType())
                                        .roleName(roleAssignment.getRoleName())
                                        .classification(roleAssignment.getClassification())
                                        .grantType(roleAssignment.getGrantType())
                                        .roleCategory(roleAssignment.getRoleCategory())
                                        .attributes(attributes)
                                        .readOnly(false).build()
                        )).build();

    }



}
