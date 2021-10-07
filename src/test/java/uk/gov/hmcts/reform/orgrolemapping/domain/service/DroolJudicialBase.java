package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_QUERY_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class DroolJudicialBase {

    StatelessKieSession kieSession;
    List<Command<?>> commands;
    ExecutionResults results;
    Set<JudicialAccessProfile> judicialAccessProfiles;
    Set<JudicialOfficeHolder> judicialOfficeHolders;

    @BeforeEach
    public void setUp() {

        judicialAccessProfiles = TestDataBuilder.buildJudicialAccessProfileSet();
        judicialOfficeHolders = TestDataBuilder.buildJudicialOfficeHolderSet();

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");

    }

    void buildExecuteKieSession(List<FeatureFlag> featureFlags, boolean accessProfile) {
        // Sequence of processing for executing the rules:
        //   1. add all the profiles
        //   2. fire all the rules
        //   3. retrieve all the created role assignments
        //      (into a variable populated by the results of a query defined in the rules).

        commands = new ArrayList<>();
        if (accessProfile) {
            commands.add(CommandFactory.newInsertElements(judicialAccessProfiles));
        } else {
            commands.add(CommandFactory.newInsertElements(judicialOfficeHolders));
        }
        commands.add(CommandFactory.newInsertElements(featureFlags));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME));

        // Run the rules
        results = kieSession.execute(CommandFactory.newBatchExecution(commands));

    }

    @NotNull
    protected List<FeatureFlag> getFeatureFlags(String flagName, Boolean status) {
        return Collections.singletonList(FeatureFlag.builder().flagName(flagName).status(status).build());
    }
}
