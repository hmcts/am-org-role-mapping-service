package uk.gov.hmcts.reform.orgrolemapping.befta;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.TestAutomationConfig;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:target/cucumber.json",
                 glue = "uk.gov.hmcts.befta.player",
                 features = {"classpath:features"})
public class OrgRoleMappingEntityApiBeftaRunner {

    private OrgRoleMappingEntityApiBeftaRunner() {
    }

    @BeforeAll
    public static void setUp() {
        BeftaMain.setUp(TestAutomationConfig.INSTANCE, new OrgRoleMappingAmTestAutomationAdapter(),
                new OrmDefaultMultiSourceFeatureToggleService());
    }

    @AfterAll
    public static void tearDown() {
        BeftaMain.tearDown();
    }
}
