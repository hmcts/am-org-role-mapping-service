package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd;

public enum JobTitle {

    // NB: for codes see:
    // https://tools.hmcts.net/confluence/pages/viewpage.action?pageId=1597738955#WorkAllocationCommonORGandCASERoles-RoleIdmappingforStaffandAdmincategory

    SENIOR_LEGAL_CASEWORKER("Senior Legal Caseworker", "1"),
    LEGAL_CASEWORKER("Legal Caseworker", "2"),
    HEARING_CENTRE_TEAM_LEADER("Hearing Centre Team Leader", "3"),
    HEARING_CENTRE_ADMIN("Hearing Centre Administrator", "4"),
    COURT_CLERK("Court Clerk", "5"),
    NBC_TEAM_LEADER("National Business Centre Team Leader", "6"),
    NBC_LISTING_TEAM("National Business Centre Listing Team", "7"),
    NBC_PAYMENTS_TEAM("National Business Centre Payments Team", "8"),
    CTSC_TEAM_LEADER("CTSC Team Leader", "9"),
    CTSC_ADMIN("CTSC Administrator", "10"),
    NBC_ADMIN("National Business Centre Administrator", "11"),
    REGIONAL_CENTRE_TEAM_LEADER("Regional Centre Team Leader", "12"),
    REGIONAL_CENTRE_ADMIN("Regional Centre Administrator", "13"),
    DWP("DWP Caseworker", "14"),
    HMRC("HMRC Caseworker", "15"),
    CICA("CICA Caseworker", "17"),
    CAFCASS("Cafcass Cymru Caseworker", "18"),
    IBCS("IBCA Caseworker", "19"),
    WLU_ADMIN("WLU Administrator", "20"),
    WLU_TEAM_LEADER("WLU Team Leader", "21"),
    HRS_TEAM_LEADER("HRS Team Leader", "22"),
    HMCTS_JUDICIARY("hmcts-Judiciary ", "23"),
    FEE_PAID_JUDGE("fee-paid-judge", "24");

    private final String roleName;
    private final String roleId;

    JobTitle(String roleName, String roleId) {
        this.roleName = roleName;
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRoleId() {
        return roleId;
    }

}
