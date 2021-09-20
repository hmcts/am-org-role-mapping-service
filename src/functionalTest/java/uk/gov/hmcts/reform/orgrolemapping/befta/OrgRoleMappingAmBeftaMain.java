package uk.gov.hmcts.reform.orgrolemapping.befta;

import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.TestAutomationConfig;

public class OrgRoleMappingAmBeftaMain {

    private OrgRoleMappingAmBeftaMain() {
    }

    public static void main(String[] args) {

        //BeftaMain.main(args, new OrgRoleMappingAmTestAutomationAdapter());
        BeftaMain.main(args, TestAutomationConfig.INSTANCE, new OrgRoleMappingAmTestAutomationAdapter(),
                OrmDefaultMultiSourceFeatureToggleService.INSTANCE);
    }
}
