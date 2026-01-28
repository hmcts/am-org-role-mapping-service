package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd;

import java.util.List;

public enum AdditionalRole implements AdditionalRoleEnum {

    // NB: for codes see:
    //  https://tools.hmcts.net/confluence/display/DTSAM/Judicial+Reconciliation#JudicialReconciliation-AdditionalRoleCodes

    ACTING_REGIONAL_EMPLOYMENT_JUDGE("Acting Regional Employment Judge", List.of("90004")),

    DESIGNATED_FAMILY_JUDGE("Designated Family Judge", List.of("90066")),
    ACTING_DESIGNATED_FAMILY_JUDGE("Acting Designated Family Judge", List.of("90154"));

    private final String name;
    private final List<String> codes; // support for multiple codes in future (i.e. different spellings for same role)

    AdditionalRole(String name, List<String> codes) {
        this.name = name;
        this.codes = codes;
    }

    public String getName() {
        return name;
    }

    public List<String> getCodes() {
        return codes;
    }

}
