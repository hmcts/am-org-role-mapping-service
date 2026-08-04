package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum CaseType {

    FR_MVP2("FinancialRemedyMVP2");

    private final String name;

    CaseType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
