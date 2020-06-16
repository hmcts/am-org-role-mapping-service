package uk.gov.hmcts.reform.orgrolemapping.befta;

import uk.gov.hmcts.befta.BeftaMain;

public class OrgRoleMappingAmBeftaMain {

    private OrgRoleMappingAmBeftaMain() {
    }

    public static void main(String[] args) {

        BeftaMain.main(args, new OrgRoleMappingAmTestAutomationAdapter());
    }
}
