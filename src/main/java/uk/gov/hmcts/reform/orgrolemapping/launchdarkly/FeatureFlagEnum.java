package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

public enum FeatureFlagEnum {
    getLDFlag("get-ld-flag"),getIACFlag("ras_drool_iac_flag_1_0"),
    getJudicialFlag("ras_drool_judicial_flag_1_0");

    private final String value;

    FeatureFlagEnum( String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }



}

