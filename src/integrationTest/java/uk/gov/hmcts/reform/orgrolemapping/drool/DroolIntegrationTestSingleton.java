package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;

import java.util.ArrayList;
import java.util.List;

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
        writeIndexFile(outputPath, judicialTests);
    }

    private void writeIndexFile(String outputFilePath, List<TestScenario> testScenarios) {
        // TODO - enhance to write full HTML index file
        String contents = "Size = " + testScenarios.size();
        createFile(outputFilePath,contents);
    }
}
