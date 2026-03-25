package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBooking;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialOfficeHolder;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRoleEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentEnum;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_QUERY_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService.ROLE_ASSIGNMENTS_RESULTS_KEY;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;

public abstract class DroolBase {

    static final String JUDICIAL_OFFICE_HOLDERS_QUERY_NAME = "getJudicialOfficeHolders";
    static final String JUDICIAL_OFFICE_HOLDERS_RESULTS_KEY = "judicialOfficeHolders";

    StatelessKieSession kieSession;
    Map<String, Set<CaseWorkerAccessProfile>> usersAccessProfiles;
    List<Command<?>> commands;
    ExecutionResults results;
    Set<CaseWorkerAccessProfile> allProfiles;
    Set<JudicialAccessProfile> judicialAccessProfiles;
    Set<JudicialOfficeHolder> judicialOfficeHolders;
    Set<JudicialBooking> judicialBookings;

    @BeforeEach
    public void setUp() {

        usersAccessProfiles = TestDataBuilder.buildUserAccessProfileMap(false, false);

        // Combine all the user profiles into a single collection for the rules engine.
        allProfiles = new HashSet<>();
        usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

        judicialAccessProfiles = TestDataBuilder.buildJudicialAccessProfileSet();
        judicialOfficeHolders = TestDataBuilder.buildJudicialOfficeHolderSet();
        judicialBookings = new HashSet<>();
        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("org-role-mapping-validation-session");

    }

    List<RoleAssignment> buildExecuteKieSession(List<FeatureFlag> featureFlags) {
        return buildExecuteKieSession(featureFlags, null);
    }

    @SuppressWarnings("unchecked")
    List<RoleAssignment> buildExecuteKieSession(List<FeatureFlag> featureFlags,
                                                List<JudicialOfficeHolder> outputJudicialOfficeHolders) {
        // Sequence of processing for executing the rules:
        //   1. add all the profiles
        //   2. fire all the rules
        //   3. retrieve all the created role assignments
        //      (into a variable populated by the results of a query defined in the rules).

        commands = List.of(CommandFactory.newInsertElements(allProfiles),
                CommandFactory.newInsertElements(judicialOfficeHolders),
                CommandFactory.newInsertElements(judicialAccessProfiles),
                CommandFactory.newInsertElements(judicialBookings),
                CommandFactory.newInsertElements(featureFlags),
                CommandFactory.newFireAllRules(),
                CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME),
                CommandFactory.newQuery(JUDICIAL_OFFICE_HOLDERS_RESULTS_KEY, JUDICIAL_OFFICE_HOLDERS_QUERY_NAME)
        );

        // Run the rules
        results = kieSession.execute(CommandFactory.newBatchExecution(commands));

        //Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        populateListFromQueryResults(
            roleAssignments,
            results,
            ROLE_ASSIGNMENTS_RESULTS_KEY,
            "$roleAssignment"
        );

        if (outputJudicialOfficeHolders != null) {
            populateListFromQueryResults(
                outputJudicialOfficeHolders,
                results,
                JUDICIAL_OFFICE_HOLDERS_RESULTS_KEY,
                "$judicialOfficeHolder"
            );
        }

        return ValidationUtil.distinctRoleAssignments(roleAssignments);
    }

    @SuppressWarnings("unchecked")
    private <T> void populateListFromQueryResults(List<T> output,
                                                  ExecutionResults results,
                                                  String resultsKey,
                                                  String rowKey) {
        QueryResults queryResults = (QueryResults) results.getValue(resultsKey);
        for (QueryResultsRow row : queryResults) {
            output.add((T) row.get(rowKey));
        }
    }

    @NotNull
    protected List<FeatureFlag> getFeatureFlags(String flagName, Boolean status) {
        return Collections.singletonList(FeatureFlag.builder().flagName(flagName).status(status).build());
    }

    @NotNull
    protected List<FeatureFlag> getAllFeatureFlagsToggleByJurisdiction(String jurisdiction, Boolean status) {
        // build list of all flags...
        return Arrays.stream(FeatureFlagEnum.values())
            .map(featureFlagEnum -> FeatureFlag.builder()
                .flagName(featureFlagEnum.getValue())
                // ... toggle those that start with jurisdiction (otherwise false)
                .status(featureFlagEnum.name().startsWith(jurisdiction.toUpperCase() + "_") ? status : false)
                .build())
            .toList();
    }

    @NotNull
    protected List<FeatureFlag> getAllFeatureFlagsToggleByJurisdiction(String jurisdiction,
                                                                       Boolean status,
                                                                       Boolean hearingFlagStatus) {
        List<FeatureFlag> featureFlags = new ArrayList<>(getAllFeatureFlagsToggleByJurisdiction(jurisdiction, status));

        for (FeatureFlag flag : featureFlags) {
            if (flag.getFlagName().contains("hearing")) {
                flag.setStatus(hearingFlagStatus);
            }
        }

        return featureFlags;
    }

    @NotNull
    protected List<FeatureFlag> getAllHearingFlags(Boolean status) {
        return Arrays.stream(FeatureFlagEnum.values())
                .map(featureFlagEnum -> FeatureFlag.builder()
                        .flagName(featureFlagEnum.getValue())
                        .status(featureFlagEnum.name().toLowerCase().contains("hearing") ? status : false)
                        .build())
                .toList();
    }

    @SneakyThrows
    public static String writeValueAsPrettyJson(Object input) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(input);
    }

    /**
     * Additional test Roles not in list of AdditionalRoles
     * <see cref="uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AdditionalRole"/>.
     */
    protected enum LegacyAdditionalRole implements AdditionalRoleEnum {

        ANY_OTHER_ROLE("Any Other Role", List.of("any-code"));

        private final String name;
        private final List<String> codes;

        LegacyAdditionalRole(String name, List<String> codes) {
            this.name = name;
            this.codes = codes;
        }

        public String getName() {
            return name;
        }

        public List<String> getCodes() {
            return codes;
        }
    }

    /**
     * Additional test Appointments not in list of Appointments
     * <see cref="uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.Appointment"/>.
     */
    protected enum LegacyAppointment implements AppointmentEnum {

        ANY_OTHER_APPOINTMENT("Any Other Appointment", List.of("any-code")),
        EMPLOYMENT_JUDGE("Employment Judge", List.of("48")),
        EMPLOYMENT_JUDGE_SITTING_IN_RETIREMENT("Employment Judge (sitting in retirement)", List.of("128", "215")),
        RECORDER("Recorder", List.of("67")),
        REGIONAL_TRIBUNAL_JUDGE("Regional Tribunal Judge", List.of("74")),
        TRIBUNAL_JUDGE("Tribunal Judge", List.of("84"));

        private final String name;
        private final List<String> codes;

        LegacyAppointment(String name, List<String> codes) {
            this.name = name;
            this.codes = codes;
        }

        public String getName() {
            return name;
        }

        public List<String> getCodes() {
            return codes;
        }
    }

}
