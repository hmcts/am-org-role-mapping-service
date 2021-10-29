package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum JudicialRolesEnum {
    RESIDENT_IMMIGRATION_JUDGE("Resident Immigration Judge"), ASSISTANT_RESIDENT_JUDGE("Assistant Resident Judge"),
    DESIGNATED_IMMIGRATION_JUDGE("Designated Immigration Judge");

    private final String value;

    JudicialRolesEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
