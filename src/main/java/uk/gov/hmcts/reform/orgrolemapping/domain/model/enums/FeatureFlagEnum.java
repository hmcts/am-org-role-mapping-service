package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_1("iac_1_1"),
    IAC_JRD_1_0("iac_jrd_1_0"),
    SSCS_HEARING_1_0("sscs_hearing_1_0"),
    SSCS_HEARING_1_1("sscs_hearing_1_1"),
    SSCS_WA_1_0("sscs_wa_1_0"),
    SSCS_WA_1_2("sscs_wa_1_2"),
    SSCS_WA_1_3("sscs_wa_1_3"),
    SSCS_WA_1_5("sscs_wa_1_5"),
    CIVIL_WA_1_0("civil_wa_1_0"),
    PRIVATELAW_HEARING_1_0("privatelaw_hearing_1_0"),
    PRIVATELAW_WA_1_0("privatelaw_wa_1_0"),
    PUBLICLAW_WA_1_0("publiclaw_wa_1_0"),
    IAC_WA_1_2("iac_wa_1_2"),
    IAC_WA_1_3("iac_wa_1_3"),
    CIVIL_WA_1_1("civil_wa_1_1"),
    PRIVATELAW_WA_1_1("privatelaw_wa_1_1"),
    EMPLOYMENT_WA_1_0("employment_wa_1_0"),
    EMPLOYMENT_WA_1_1("employment_wa_1_1"),
    EMPLOYMENT_WA_1_2("employment_wa_1_2"),
    EMPLOYMENT_WA_1_3("employment_wa_1_3"),
    EMPLOYMENT_WA_1_4("employment_wa_1_4"),
    EMPLOYMENT_WA_1_5("employment_wa_1_5"),
    PRIVATELAW_WA_1_2("privatelaw_wa_1_2"),
    ST_CIC_WA_1_0("st_cic_wa_1_0"),
    ST_CIC_WA_1_1("st_cic_wa_1_1"),
    ST_CIC_WA_1_2("st_cic_wa_1_2"),
    PUBLICLAW_WA_1_1("publiclaw_wa_1_1"),
    CIVIL_WA_1_2("civil_wa_1_2"),
    CIVIL_WA_1_3("civil_wa_1_3"),
    PRIVATELAW_WA_1_3("privatelaw_wa_1_3"),
    PRIVATELAW_WA_1_4("privatelaw_wa_1_4"),
    PRIVATELAW_WA_1_5("privatelaw_wa_1_5"),
    PRIVATELAW_WA_1_6("privatelaw_wa_1_6"),
    PRIVATELAW_WA_1_7("privatelaw_wa_1_7"),
    PRIVATELAW_WA_1_8("privatelaw_wa_1_8"),
    PUBLICLAW_WA_1_2("publiclaw_wa_1_2"),
    CIVIL_WA_1_4("civil_wa_1_4"),
    CIVIL_WA_1_5("civil_wa_1_5"),
    CIVIL_WA_1_6("civil_wa_1_6"),
    CIVIL_WA_1_7("civil_wa_1_7"),
    CIVIL_WA_1_8("civil_wa_1_8"),
    CIVIL_WA_1_9("civil_wa_1_9"),
    CIVIL_WA_2_0("civil_wa_2_0"),
    CIVIL_WA_2_1("civil_wa_2_1"),
    CIVIL_WA_2_2("civil_wa_2_2"),
    CIVIL_WA_2_3("civil_wa_2_3"),
    CIVIL_WA_2_4("civil_wa_2_4"),
    CIVIL_WA_2_5("civil_wa_2_5"),
    PUBLICLAW_WA_1_3("publiclaw_wa_1_3"),
    PUBLICLAW_WA_1_4("publiclaw_wa_1_4"),
    PUBLICLAW_WA_1_5("publiclaw_wa_1_5"),
    PUBLICLAW_WA_1_6("publiclaw_wa_1_6"),
    PUBLICLAW_WA_1_7("publiclaw_wa_1_7"),
    PUBLICLAW_WA_1_8("publiclaw_wa_1_8"),
    PUBLICLAW_WA_1_9("publiclaw_wa_1_9"),
    PUBLICLAW_WA_2_0("publiclaw_wa_2_0"),
    PUBLICLAW_WA_2_1("publiclaw_wa_2_1"),
    IAC_JRD_1_1("iac_jrd_1_1"),
    PUBLICLAW_HEARING_1_0("publiclaw_hearing_1_0");


    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
