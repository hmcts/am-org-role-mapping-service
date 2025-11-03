package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildBulletPoints;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHeading2;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlPage;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHyperlink;
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
        buildJudicialFiles(outputPath, judicialTests).forEach((fileName, htmlBody) -> {
            String title = "Drool Judicial Test Report";
            createFile(outputPath + fileName, buildHtmlPage(fileName, title, htmlBody));
        });
    }

    public static Map<String,String> buildJudicialFiles(String outputPath, List<TestScenario> testScenarios) {
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
                results.put(entry.getKey(), buildHtmlBody(outputPath, entry.getValue())));

        return results;
    }

    private static String buildHtmlBody(String outputPath, List<TestScenario> testScenarios) {
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

            // Add the files in folder (as bullet pointed hyperlinks
            String bulletPoints = "";
            for (String filename : getFilesInFolder(testScenario.getOutputLocation())) {
                // Add the file as a hyperlink (minus the relative path to the output folder)
                bulletPoints += buildLine(
                        buildHyperlink(testScenario.getOutputLocation().replace(outputPath,"")
                                + filename, filename));
            }
            body += buildBulletPoints(bulletPoints);
        }
        return body;
    }

    private static List<String> getFilesInFolder(final String outputLocation) {
        List<String> result = new ArrayList<>();
        Arrays.stream(new File(outputLocation).listFiles())
                .toList().forEach(file -> {
                    result.add(file.getName());
                });
        return result;
    }
}
