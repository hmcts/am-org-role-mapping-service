package uk.gov.hmcts.reform.orgrolemapping.drool;

import org.apache.commons.lang3.StringUtils;
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
    private static final String FEATUREFLAGS_FILENAME = "FeatureFlags.json";
    private static final String HAPPY_PATH = "HappyPath";
    private static final String HTML_FILENAME_SUFFIX = ".html";
    private static final String INPUT = "Input";
    private static final String JUDICIAL_FILENAME_PREFIX = "JudicialTest_";
    private static final String JUDICIAL_INDEX_FILENAME = "JudicialTestIndex.html";
    private static final String NEGATIVE_TEST = "NegativeTest";
    private static final String OTHER = "Other";
    private static final String OUTPUT = "Output";
    private static final String RED =  "red";
    private static final String REQUEST =  "request";
    private static final String RESPONSE = "response";
    private static final String TESTARGUMENTS_FILENAME = "TestArguments.json";
    
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

        // Output the Featureflags / test arguments link
        body.append(buildParagraph(buildHtmlTestNameLinks(outputPath,
                map.values().stream().findFirst().orElse("")), null));

        for (Map.Entry entry : map.entrySet()) {
            String description = (String) entry.getKey();
            String outputLocation = (String) entry.getValue();

            // Check for any description errors ...
            Error error = getInstance().judicialErrors.containsKey(testGroup)
                    && getInstance().judicialErrors.get(testGroup).containsKey(testName)
                    ? getInstance().judicialErrors.get(testGroup).get(testName).get(description) : null;

            String descriptionColour = error != null ? RED : null;

            // Get the filesInFolder
            List<String> filesInFolder = getFilesInFolder(outputLocation);
            Map<String, String> fileInFolderMap = categoriseFiles(filesInFolder);
            boolean testSkipped = fileInFolderMap.isEmpty()
                    || (!fileInFolderMap.containsKey(OUTPUT) && !fileInFolderMap.containsKey(OTHER));

            // Output the description collapsible section
            body.append(buildContents(description + (testSkipped ? " - Skipped" : ""),
                    buildContentsOfFolder(fileInFolderMap, outputPath, outputLocation), descriptionColour, error));
        }
        return body.toString();
    }

    private static String buildHtmlTestNameLinks(String outputPath, String testScenarioOutputLocation) {
        StringBuilder body = new StringBuilder();
        String testArgumentsLocation = getTestArgumentsLocation(outputPath,testScenarioOutputLocation);
        body.append(buildLine(buildHyperlink(testArgumentsLocation + FEATUREFLAGS_FILENAME, FEATUREFLAGS_FILENAME)));
        body.append(buildLine(buildHyperlink(testArgumentsLocation + TESTARGUMENTS_FILENAME, TESTARGUMENTS_FILENAME)));
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

    private static String buildContentsOfFolder(Map<String, String> fileInFolderMap,
                                                String outputPath, String outputLocation) {
        // Get the list of hyperlinks
        List<String> outputHyperLinks = new ArrayList<>();
        List<String> inputHyperLinks = new ArrayList<>();
        List<String> otherHyperLinks = new ArrayList<>();
        fileInFolderMap.entrySet().forEach(entry -> {
            String category = entry.getKey();
            String filename = entry.getValue();
            // build the hyperlink (minus the relative path to the output folder)
            String filePath = outputLocation.replace(outputPath,"") + filename;
            String hyperlink = buildHyperlink(filePath, filename);
            if (INPUT.equals(category)) {
                inputHyperLinks.add(hyperlink);
            } else if (OUTPUT.equals(category)) {
                outputHyperLinks.add(hyperlink);
            } else {
                otherHyperLinks.add(hyperlink);
            }
        });

        String lineFormat = "%s - %s";
        return buildLine(String.format(lineFormat,INPUT,buildStringList(inputHyperLinks)))
                + buildLine(String.format(lineFormat,OUTPUT,buildStringList(outputHyperLinks)))
                + buildLine(String.format(lineFormat,OTHER,buildStringList(otherHyperLinks)));
    }

    private static Map<String,String> categoriseFiles(List<String> files) {
        Map<String,String> fileInFolderMap = new HashMap<>();
        files.forEach(filename -> {
            if (isInputFileName(filename)) {
                fileInFolderMap.put(INPUT, filename);
            } else if (isOutputFileName(filename)) {
                fileInFolderMap.put(OUTPUT, filename);
            } else {
                fileInFolderMap.put(OTHER, filename);
            }
        });
        return fileInFolderMap;
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
        File[] files = new File(outputLocation).listFiles();
        if (files != null) {
            Arrays.stream(files).sorted()
                    .toList().forEach(file -> {
                        if (isFileValid(file)) {
                            result.add(file.getName());
                        }
                    });
        }
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

    private static String getTestArgumentsLocation(String outputPath, String testScenarioOutputLocation) {
        if (!StringUtils.isEmpty(testScenarioOutputLocation)) {
            int pos = testScenarioOutputLocation.lastIndexOf(HAPPY_PATH);
            if (pos == -1) {
                pos = testScenarioOutputLocation.lastIndexOf(NEGATIVE_TEST);
            }
            if (pos != -1) {
                return testScenarioOutputLocation.substring(0, pos).replace(outputPath,"");
            }
        }
        return null;
    }
}
