package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_CONTENT_STYLE_CLASS;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_SCRIPT;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_STYLE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_HEADER_STYLE_CLASS;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildBulletPoints;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildButton;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildDiv;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHeading2;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlPage;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHyperlink;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildLine;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.createFile;

@SuppressWarnings("unchecked")
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
            createFile(outputPath + fileName, buildHtmlPage(fileName,
                    COLLAPSE_STYLE, title, htmlBody, COLLAPSE_SCRIPT));
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
        String body = "";

        // Build the grouping map
        Map<String, Map<String, Map<String, String>>> groupingMap =
                buildGroupingMap(testScenarios);

        // Loop the test groups
        for (Map.Entry testGroupMap : groupingMap.entrySet()) {
            String testGroup = (String) testGroupMap.getKey();
            Map<String, Map<String, String>> testNamesMap =
                    (Map<String, Map<String, String>>) testGroupMap.getValue();

            // Output the test group heading
            body += buildHeading2(testGroup);

            // Output the test names
            body += buildHtmlTestNames(outputPath, testNamesMap);
        }

        return body;
    }

    private static String buildHtmlTestNames(
            String outputPath, Map<String, Map<String, String>> map) {
        String body = "";
        for (Map.Entry entry : map.entrySet()) {
            String testName = (String) entry.getKey();
            Map<String, String> descriptionsMap =
                    (Map<String, String>) entry.getValue();

            // Output the test name callapsible section
            body += buildContents(testName,
                    buildHtmlDescriptions(outputPath, descriptionsMap));
        }
        return body;
    }

    private static String buildHtmlDescriptions(
            String outputPath, Map<String, String> map) {
        String body = "";
        for (Map.Entry entry : map.entrySet()) {
            String description = (String) entry.getKey();
            String outputLocation = (String) entry.getValue();

            // Output the description
            body += buildContents(description,
                    buildContentsOfFolder(outputPath, outputLocation));
        }
        return body;
    }

    private static Map<String, Map<String, Map<String, String>>> buildGroupingMap(
            List<TestScenario> testScenarios) {
        Map<String, Map<String, Map<String, String>>> groupingMap = new HashMap<>();
        for (TestScenario testScenario: testScenarios) {
            // Add the test groups
            groupingMap
                    .computeIfAbsent(testScenario.getTestGroup(), k -> new HashMap<>());
            // Add the test names
            groupingMap.get(testScenario.getTestGroup())
                    .computeIfAbsent(testScenario.getTestName(), k -> new HashMap<>());
            // Add the descriptions
            groupingMap.get(testScenario.getTestGroup()).get(testScenario.getTestName())
                    .put(testScenario.getDescription(), testScenario.getOutputLocation());
        }
        return groupingMap;
    }

    private static String buildContents(String heading, String contents) {
        String body = buildButton(COLLAPSE_HEADER_STYLE_CLASS, heading);
        body += buildDiv(COLLAPSE_CONTENT_STYLE_CLASS, contents);
        return body;
    }

    private static String buildContentsOfFolder(String outputPath, String outputLocation) {
        String contents = "";
        for (String filename : getFilesInFolder(outputLocation)) {
            // Add the file as a hyperlink (minus the relative path to the output folder)
            contents += buildLine(
                    buildHyperlink(outputLocation.replace(outputPath,"")
                            + filename, filename));
        }
        return buildBulletPoints(contents);
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
