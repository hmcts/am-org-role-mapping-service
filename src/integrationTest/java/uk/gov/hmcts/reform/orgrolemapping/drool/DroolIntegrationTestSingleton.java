package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_CONTENT_DIV_CLASS;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_SCRIPT;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.COLLAPSE_STYLE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.CROSS;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildButton;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildDiv;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHeading2;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHtmlPage;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildHyperlink;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildLine;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildParagraph;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.buildTickOrCross;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.getCollapseHeaderStyleClass;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.getCollapseStyle;
import static uk.gov.hmcts.reform.orgrolemapping.drool.HtmlBuilder.makeHtmlSafe;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.createFile;

@SuppressWarnings("unchecked")
public class DroolIntegrationTestSingleton  {

    private static final String EXPECTED = "expected";
    private static final String ERROR_PREFIX = CROSS + " Error: ";
    private static final String HTML_FILENAME_SUFFIX = ".html";
    private static final String JUDICIAL_FILENAME_PREFIX = "JudicialTest_";
    private static final String JUDICIAL_INDEX_FILENAME = "JudicialTestIndex.html";
    private static final String RED =  "red";
    private static final String REQUEST =  "request";
    private static final String RESPONSE = "response";
    
    private static DroolIntegrationTestSingleton instance = null;

    public List<TestScenario> judicialTests = new ArrayList<>();
    public Map<String, Map<String, Map<String, Error>>> judicialErrors = new LinkedHashMap<>();

    public static DroolIntegrationTestSingleton getInstance() {
        if (instance == null) {
            instance = new DroolIntegrationTestSingleton();
        }
        return instance;
    }

    public void addJudicialError(TestScenario testScenario, Error error) {
        judicialErrors
                .computeIfAbsent(testScenario.getTestGroup(), k1 -> new LinkedHashMap<>())
                    .computeIfAbsent(testScenario.getTestName(), k2 -> new LinkedHashMap<>())
                        .put(testScenario.getDescription(), error);
    }

    public void writeJudicialIndexFile(String outputPath) {
        final String title = "Drool Judicial Test Report";

        // Build the Judicial Files by Jurisdiction.
        Map<String, Boolean> indexLinks = new HashMap<>();
        buildJudicialFiles(outputPath, judicialTests).forEach((jurisdiction, htmlBody) -> {
            String fileName = buildFilename(jurisdiction);
            createFile(outputPath + fileName, buildHtmlPage(fileName,
                    COLLAPSE_STYLE, String.format("%s - %s", title, jurisdiction),
                    htmlBody, COLLAPSE_SCRIPT));
            indexLinks.put(fileName, !htmlBody.contains(ERROR_PREFIX));
        });

        // Build the Index page for all Jurisdiction files.
        createFile(outputPath + JUDICIAL_INDEX_FILENAME, buildHtmlPage(
                JUDICIAL_INDEX_FILENAME, "",
                "Index of Judicial Test Reports", buildHtmlIndexes(indexLinks), ""));
    }

    private static String buildHtmlIndexes(Map<String, Boolean> indexLinks) {
        StringBuilder body = new StringBuilder();
        indexLinks.entrySet().forEach(entry -> {
            String url = entry.getKey();
            String linkText = String.format("%s%s",
                    buildTickOrCross(entry.getValue()), entry.getKey());
            body.append(buildLine(buildHyperlink(url, linkText)));
        });
        return body.toString();
    }

    public static Map<String,String> buildJudicialFiles(String outputPath, List<TestScenario> testScenarios) {
        // Sort the data by Jurisdiction, test group, test name and description
        testScenarios.sort(Comparator.comparing(TestScenario::getJurisdiction)
                .thenComparing(TestScenario::getTestGroup)
                .thenComparing(TestScenario::getTestName)
                .thenComparing(TestScenario::getDescription));

        // Build a map of file names to test scenarios
        Map<String, List<TestScenario>> map = new LinkedHashMap<>();
        testScenarios.forEach(testScenario ->
                map.computeIfAbsent(testScenario.getJurisdiction(),
                        k -> new ArrayList<>()).add(testScenario));

        // Build the map of file names to HTML content
        Map<String, String> results = new LinkedHashMap<>();
        map.entrySet().forEach(entry ->
                results.put(entry.getKey(), buildHtmlBody(outputPath, entry.getValue())));

        return results;
    }

    private static String buildFilename(String jurisdiction) {
        return JUDICIAL_FILENAME_PREFIX + jurisdiction + HTML_FILENAME_SUFFIX;
    }

    private static String buildHtmlBody(String outputPath, List<TestScenario> testScenarios) {

        // Build the grouping map
        Map<String, Map<String, Map<String, String>>> groupingMap =
                buildGroupingMap(testScenarios);

        // Loop the test groups
        StringBuilder body = new StringBuilder();
        for (Map.Entry testGroupMap : groupingMap.entrySet()) {
            String testGroup = (String) testGroupMap.getKey();
            Map<String, Map<String, String>> testNamesMap =
                    (Map<String, Map<String, String>>) testGroupMap.getValue();

            // Check for any testGroup errors and highlight in RED
            String groupColour = getInstance().judicialErrors.containsKey(testGroup) ? RED : null;

            // Output the test group heading (ie 001_Circuit_Judge__Salaried)
            body.append(buildHeading2(testGroup, groupColour));

            // Output the test names (ie 001_Circuit_Judge__Salaried__SALARIED)
            body.append(buildHtmlTestNames(testGroup, outputPath, testNamesMap));
        }

        return body.toString();
    }

    private static String buildHtmlTestNames(String testGroup,
            String outputPath, Map<String, Map<String, String>> map) {
        StringBuilder body = new StringBuilder();
        for (Map.Entry entry : map.entrySet()) {
            String testName = (String) entry.getKey();
            Map<String, String> descriptionsMap =
                    (Map<String, String>) entry.getValue();

            // Check for any testName errors and highlight in RED
            String nameColour = getInstance().judicialErrors.containsKey(testGroup)
                    && getInstance().judicialErrors.get(testGroup).containsKey(testName)
                    ? RED : null;

            // Output the test name collapsible section
            body.append(buildContents(testName,
                    buildHtmlDescriptions(testGroup, testName, outputPath, descriptionsMap),
                    nameColour, null));
        }
        return body.toString();
    }

    private static String buildError(Error error) {
        StringBuilder body = new StringBuilder();
        body.append(ERROR_PREFIX)
            .append(makeHtmlSafe(error.getMessage()));
        return body.toString();
    }

    private static String buildHtmlDescriptions(String testGroup, String testName,
            String outputPath, Map<String, String> map) {
        StringBuilder body = new StringBuilder();

        for (Map.Entry entry : map.entrySet()) {
            String description = (String) entry.getKey();
            String outputLocation = (String) entry.getValue();

            // Check for any description errors ...
            Error error = getInstance().judicialErrors.containsKey(testGroup)
                    && getInstance().judicialErrors.get(testGroup).containsKey(testName)
                    ? getInstance().judicialErrors.get(testGroup).get(testName).get(description) : null;

            String descriptionColour = error != null ? RED : null;

            // Output the description collapsible section
            body.append(buildContents(description,
                    buildContentsOfFolder(outputPath, outputLocation), descriptionColour, error));
        }
        return body.toString();
    }

    private static Map<String, Map<String, Map<String, String>>> buildGroupingMap(
            List<TestScenario> testScenarios) {

        // Build the grouping map
        Map<String, Map<String, Map<String, String>>> groupingMap = new LinkedHashMap<>();
        for (TestScenario testScenario: testScenarios) {
            // Add the test groups (ie 001_Circuit_Judge__Salaried)
            groupingMap
                    .computeIfAbsent(testScenario.getTestGroup(), k -> new LinkedHashMap<>());
            // Add the test names (ie 001_Circuit_Judge__Salaried__SALARIED)
            groupingMap.get(testScenario.getTestGroup())
                    .computeIfAbsent(testScenario.getTestName(), k -> new LinkedHashMap<>());
            // Add the descriptions (ie HappyPath - all dates supplied - WithBooking)
            groupingMap.get(testScenario.getTestGroup()).get(testScenario.getTestName())
                    .put(testScenario.getDescription(), testScenario.getOutputLocation());
        }
        return groupingMap;
    }

    private static String buildContents(String heading, String contents, String errorColour, Error error) {
        StringBuilder body = new StringBuilder();

        boolean active = errorColour != null;
        body.append(buildButton(getCollapseHeaderStyleClass(active), heading, errorColour));

        StringBuilder bodyContents = new StringBuilder();
        // if there is an error to report then show that in the contents
        if (error != null) {
            bodyContents.append(buildParagraph(buildError(error), errorColour));
        }
        bodyContents.append(contents);

        body.append(buildDiv(getCollapseStyle(active),
                COLLAPSE_CONTENT_DIV_CLASS, bodyContents.toString()));

        return body.toString();
    }

    private static String buildContentsOfFolder(String outputPath, String outputLocation) {
        // Get the list of hyperlinks
        List<String> outputHyperLinks = new ArrayList<>();
        List<String> inputHyperLinks = new ArrayList<>();
        List<String> otherHyperLinks = new ArrayList<>();
        getFilesInFolder(outputLocation).forEach(filename -> {
            // build the hyperlink (minus the relative path to the output folder)
            String filePath = outputLocation.replace(outputPath,"") + filename;
            String hyperlink = buildHyperlink(filePath, filename);
            if (isInputFileName(filename)) {
                inputHyperLinks.add(hyperlink);
            } else if (isOutputFileName(filename)) {
                outputHyperLinks.add(hyperlink);
            } else {
                otherHyperLinks.add(hyperlink);
            }
        });

        String lineFormat = "%s - %s";
        return buildLine(String.format(lineFormat,"Input",buildStringList(inputHyperLinks)))
                + buildLine(String.format(lineFormat,"Output",buildStringList(outputHyperLinks)))
                + buildLine(String.format(lineFormat,"Other",buildStringList(otherHyperLinks)));
    }

    private static String buildStringList(List<String> list) {
        StringBuilder result = new StringBuilder();
        list.forEach(entry -> {
            result.append(result.length() > 0 ? ", " : "").append(entry);
        });
        return result.toString();
    }

    private static List<String> getFilesInFolder(final String outputLocation) {
        List<String> result = new ArrayList<>();
        Arrays.stream(new File(outputLocation).listFiles()).sorted()
                .toList().forEach(file -> {
                    if (isFileValid(file)) {
                        result.add(file.getName());
                    }
                });
        return result;
    }

    private static boolean isInputFileName(String file) {
        return file.toLowerCase(Locale.getDefault()).contains(RESPONSE);
    }

    private static boolean isOutputFileName(String file) {
        return file.toLowerCase(Locale.getDefault()).contains(REQUEST);
    }

    private static boolean isFileValid(File file) {
        return file != null && !file.getName().toLowerCase(Locale.getDefault())
                .contains(EXPECTED);
    }
}
