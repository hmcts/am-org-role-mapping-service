package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum FeatureFlagEnum {
    IAC_1_0("iac_1_0"), IAC_1_1("iac_1_1");

    private final String value;

    FeatureFlagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}