package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

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
        writeIndexFile("Drool Judicial Test Report", outputPath, judicialTests);
    }

    private void writeIndexFile(String title,
                                String outputFilePath,
                                List<TestScenario> testScenarios) {
        String group = "";
        String body = "<ul>\n";
        for (TestScenario testScenario : testScenarios) {
            if (!testScenario.getTestGroup().equals(group)) {
                group = testScenario.getTestGroup();
                body += buildHeading2(group);
            }
            body += buildLine(buildHyperlink(
                    testScenario.getOutputLocation(),
                    testScenario.getDescription()
            ));
        }
        body += "</ul>\n";
        createFile(outputFilePath, buildHtmlPage(title, title, body));
    }
}
