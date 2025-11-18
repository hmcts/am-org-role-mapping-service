package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.controller.FeatureFlagController;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.DROOL_TEST_OUTPUT_PATH;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_SCRIPT;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_STYLE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlPage;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlTable;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.createFile;

public class FeatureFlagReportSingleton {

    private static final String FILENAME = "FeatureFlagReport.html";
    public static final String OUTPUT_PATH = DROOL_TEST_OUTPUT_PATH;

    private static FeatureFlagReportSingleton instance = null;

    public static FeatureFlagReportSingleton getInstance() {
        if (instance == null) {
            instance = new FeatureFlagReportSingleton();
        }
        return instance;
    }

    public void generateReport(FeatureFlagController featureFlagController) {
        String title = "Feature Flag Report";

        // Build the html body
        String htmlBody = buildFeatureFlagTable(
                featureFlagController.getAllFeatureFlags(null).getBody()
        );

        // Generate the report
        createFile(OUTPUT_PATH + FILENAME, buildHtmlPage(FILENAME,
                COLLAPSE_STYLE, title, htmlBody, COLLAPSE_SCRIPT));
    }

    private String buildFeatureFlagTable(Map<String, Boolean> featureFlags) {
        List<List<String>> tableRows = featureFlags.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> List.of(entry.getKey(), entry.getValue().toString()))
                .toList();
        return buildHtmlTable(List.of("Feature Flag", "Enabled"), tableRows);
    }
}
