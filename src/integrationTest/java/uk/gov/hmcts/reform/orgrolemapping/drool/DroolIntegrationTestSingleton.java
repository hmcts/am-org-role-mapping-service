package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHeading2;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlPage;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildLine;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.createFile;

public class DroolIntegrationTestSingleton  {

    private static DroolIntegrationTestSingleton instance = null;

    public List<TestScenario> judicialTests = new ArrayList<>();

    public static DroolIntegrationTestSingleton getInstance() {
        if (instance == null) {
            instance = new DroolIntegrationTestSingleton();
        }
        return instance;
    }

    public void writeJudicialIndexFile(String outputPath) {
        buildJudicialFiles(judicialTests).forEach((fileName, htmlBody) -> {
            String title = "Drool Judicial Test Report";
            createFile(outputPath + fileName, buildHtmlPage(fileName, title, htmlBody));
        });
    }

    public static Map<String,String> buildJudicialFiles(List<TestScenario> testScenarios) {
        // Sort the data by test group and description
        testScenarios.sort(Comparator.comparing(TestScenario::getTestGroup)
                .thenComparing(TestScenario::getDescription));

        // Build a map of file names to test scenarios
        Map<String, List<TestScenario>> map = new HashMap<>();
        testScenarios.forEach(testScenario ->
                map.computeIfAbsent("JudicialTest_" + testScenario.getJurisdiction() + ".html",
                        k -> new ArrayList<>()).add(testScenario));

        // Build the map of file names to HTML content
        Map<String, String> results = new HashMap<>();
        map.entrySet().forEach(entry ->
                results.put(entry.getKey(), buildHtmlBody(entry.getValue())));

        return results;
    }

    private static String buildHtmlBody(List<TestScenario> testScenarios) {
        String group = "";
        String body = "";
        for (TestScenario testScenario : testScenarios) {
            // Add the group heading if it has changed
            if (!testScenario.getTestGroup().equals(group)) {
                group = testScenario.getTestGroup();
                body += buildHeading2(group);
            }
            // Add the test scenario line
            body += buildLine(testScenario.getTestName());
            //TODO add the files from the output directory as links
        }
        return body;
    }
}
