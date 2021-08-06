package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_QUERY_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

public abstract class DroolBase {

    StatelessKieSession kieSession;
    Map<String, Set<CaseWorkerAccessProfile>> usersAccessProfiles;
    List<Command<?>> commands;
    ExecutionResults results;
    Set<CaseWorkerAccessProfile> allProfiles;

    @BeforeEach
    public void setUp() {

        usersAccessProfiles = TestDataBuilder.buildUserAccessProfileMap(false, false);

        // Combine all the user profiles into a single collection for the rules engine.
        allProfiles = new HashSet<>();
        usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");

    }

    void buildExecuteKieSession(List<FeatureFlag> featureFlags) {
        // Sequence of processing for executing the rules:
        //   1. add all the profiles
        //   2. fire all the rules
        //   3. retrieve all the created role assignments
        //      (into a variable populated by the results of a query defined in the rules).

        commands = new ArrayList<>();
        commands.add(CommandFactory.newInsertElements(allProfiles));
        commands.add(CommandFactory.newInsertElements(featureFlags));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME));

        // Run the rules
        results = kieSession.execute(CommandFactory.newBatchExecution(commands));

    }

}
