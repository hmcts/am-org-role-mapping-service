package uk.gov.hmcts.reform.orgrolemapping.befta;

import io.cucumber.java.Scenario;
import uk.gov.hmcts.befta.featuretoggle.DefaultMultiSourceFeatureToggleService;
import uk.gov.hmcts.befta.featuretoggle.FeatureToggleService;
import uk.gov.hmcts.befta.featuretoggle.ScenarioFeatureToggleInfo;

import java.util.stream.Stream;

public class OrmDefaultMultiSourceFeatureToggleService extends DefaultMultiSourceFeatureToggleService {

    private static final String LAUNCH_DARKLY_FLAG = "FeatureToggle";
    public static final OrmDefaultMultiSourceFeatureToggleService INSTANCE
        = new OrmDefaultMultiSourceFeatureToggleService();

    @Override
    @SuppressWarnings("unchecked")
    public ScenarioFeatureToggleInfo getToggleStatusFor(Scenario toggleable) {
        ScenarioFeatureToggleInfo scenarioFeatureToggleInfo = new ScenarioFeatureToggleInfo();
        //@FeatureToggle(LD:feature_id_1=on) @FeatureToggle(IAC:feature_id_2=off)
        toggleable.getSourceTagNames().stream().filter(tag -> tag.contains(LAUNCH_DARKLY_FLAG)).forEach(tag -> {

            String id = null;
            String domain = tag.contains(COLON) ? tag.substring(tag.indexOf("(") + 1, tag.indexOf(COLON)) : "LD";
            FeatureToggleService service = getToggleService(domain);

            if (!tag.contains(COLON) && !tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf("(") + 1, tag.indexOf(")"));
            } else if (tag.contains(COLON) && !tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf(COLON) + 1, tag.indexOf(")"));
            } else if (tag.contains(COLON) && tag.contains(STRING_EQUALS)) {
                id = tag.substring(tag.indexOf(COLON) + 1, tag.indexOf(STRING_EQUALS));
            }

            if (tag.contains(STRING_EQUALS)) {
                String expectedStatusString = tag.substring(tag.indexOf(STRING_EQUALS) + 1, tag.indexOf(")"));
                Boolean expectedStatus = expectedStatusString.equalsIgnoreCase("on");
                scenarioFeatureToggleInfo.addExpectedStatus(id, expectedStatus);
            }
            Boolean actualStatus;
            actualStatus = (Boolean) service.getToggleStatusFor(id);
            scenarioFeatureToggleInfo.addActualStatus(id, actualStatus);
        });
        return scenarioFeatureToggleInfo;
    }

    @Override
    protected FeatureToggleService getToggleService(String toggleDomain) {
        if (Stream.of("IAC", "RAS", "DB").anyMatch(toggleDomain::equalsIgnoreCase)) {
            return new uk.gov.hmcts.reform.orgrolemapping.befta.OrmFeatureToggleService();
        } else {
            return super.getToggleService(toggleDomain);
        }
    }
}
