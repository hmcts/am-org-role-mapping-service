package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_1("iac_1_1"),
    IAC_JRD_1_0("iac_jrd_1_0"),
    SSCS_HEARING_1_0("sscs_hearing_1_0"),
    SSCS_WA_1_0("sscs_wa_1_0"),
    CIVIL_WA_1_0("civil_wa_1_0"),
    PRIVATELAW_WA_1_0("privatelaw_wa_1_0");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}