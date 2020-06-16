package uk.gov.hmcts.reform.orgrolemapping.befta;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import uk.gov.hmcts.befta.BeftaMain;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = "json:target/cucumber.json",
                 glue = "uk.gov.hmcts.befta.player",
                 features = {"classpath:features"})
public class OrgRoleMappingEntityApiBeftaRunner {

    private OrgRoleMappingEntityApiBeftaRunner() {
    }

    @BeforeClass
    public static void setUp() {
        BeftaMain.setUp(new OrgRoleMappingAmTestAutomationAdapter());
    }

    @AfterClass
    public static void tearDown() {
        BeftaMain.tearDown();
    }

}
