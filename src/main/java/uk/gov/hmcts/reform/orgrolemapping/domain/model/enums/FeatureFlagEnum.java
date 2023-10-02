package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_1("iac_1_1"),
    IAC_JRD_1_0("iac_jrd_1_0"),
    SSCS_HEARING_1_0("sscs_hearing_1_0"),
    SSCS_WA_1_0("sscs_wa_1_0"),
    CIVIL_WA_1_0("civil_wa_1_0"),
    PRIVATELAW_WA_1_0("privatelaw_wa_1_0"),
    PUBLICLAW_WA_1_0("publiclaw_wa_1_0"),
    IAC_WA_1_2("iac_wa_1_2"),
    CIVIL_WA_1_1("civil_wa_1_1"),
    PRIVATELAW_WA_1_1("privatelaw_wa_1_1"),
    EMPLOYMENT_WA_1_0("employment_wa_1_0"),
    PRIVATELAW_WA_1_2("privatelaw_wa_1_2"),
    ST_CIC_WA_1_0("st_cic_wa_1_0"),
    PUBLICLAW_WA_1_1("publiclaw_wa_1_1"),
    CIVIL_WA_1_2("civil_wa_1_1");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
