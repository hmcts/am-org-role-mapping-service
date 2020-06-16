package uk.gov.hmcts.reform.orgrolemapping.befta;

public class TestDataLoaderMain {

    private TestDataLoaderMain() {
    }

    public static void main(String[] args) {
        new OrgRoleMappingAmTestAutomationAdapter().doLoadTestData();
    }

}
