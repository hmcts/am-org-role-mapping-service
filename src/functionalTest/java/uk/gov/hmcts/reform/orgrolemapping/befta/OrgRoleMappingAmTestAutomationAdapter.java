package uk.gov.hmcts.reform.orgrolemapping.befta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;

import java.util.UUID;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;

public class OrgRoleMappingAmTestAutomationAdapter extends DefaultTestAutomationAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OrgRoleMappingAmTestAutomationAdapter.class);

    public static final String EMAIL_TEMPLATE = "ORM-func-test-user-%s@justice.gov.uk";

    @Override
    public Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        //the docAMUrl is is referring the self link in PR
        switch (key.toString()) {
            case ("generateUUID"):
                return UUID.randomUUID();
            case ("generateEmailId"):
                return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
            case ("waitForTime"):
                logger.info("Sleeping for 5 seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException exception) {
                    logger.info(exception.getMessage());
                }
                logger.info("The nap is complete.");
                return null;
            case ("waitForTime20"):
                logger.info("Sleeping for 20 seconds");
                try {
                    Thread.sleep(25000);
                } catch (InterruptedException exception) {
                    logger.info(exception.getMessage());
                }
                logger.info("The nap is complete.");
                return null;
            case ("generateCRDServiceBusToken"):
                try {
                    return FunctionalTestUtils.getSaSToken("sb://"
                                    + EnvironmentVariableUtils.getRequiredVariable("AMQP_HOST"),
                            "SendAndListenSharedAccessKey",
                            EnvironmentVariableUtils.getRequiredVariable("AMQP_CRD_SHARED_ACCESS_KEY_VALUE"));
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
                return null;
            case ("generateJRDServiceBusToken"):
                try {
                    return FunctionalTestUtils.getSaSToken("sb://"
                           + EnvironmentVariableUtils.getRequiredVariable("AMQP_HOST"),
                           "SendAndListenSharedAccessKey",
                            EnvironmentVariableUtils.getRequiredVariable("AMQP_JRD_SHARED_ACCESS_KEY_VALUE"));
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
                logger.info("The nap is complete.");
                return null;
            default:
                return super.calculateCustomValue(scenarioContext, key);
        }
    }
}
