package uk.gov.hmcts.reform.orgrolemapping.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.TestDataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.UUID;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

public class OrgRoleMappingAmTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OrgRoleMappingAmTestAutomationAdapter.class);

    private TestDataLoaderToDefinitionStore loader = new TestDataLoaderToDefinitionStore(this);

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@cwrfunctestuser.com";

    @Override
    public void doLoadTestData() {

    }

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        //the docAMUrl is is referring the self link in PR
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateEmailId"):
                String s = String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
                logger.info("generate email id: " + s);
                return s;
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }
}
