package uk.gov.hmcts.reform.orgrolemapping.befta;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.util.UUID;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

public class OrgRoleMappingAmTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OrgRoleMappingAmTestAutomationAdapter.class);

    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@cwrfunctestuser.com";

    @SneakyThrows
    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        //the docAMUrl is is referring the self link in PR
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateEmailId"):
                return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
            case ("waitForTime"):
                logger.info("Sleeping for 10 seconds");
                Thread.sleep(10000);
                logger.info("The nap is complete.");
                return null;
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }
}
