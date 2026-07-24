package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum OrganisationStatus {
    ACTIVE("ACTIVE"),
    BLOCKED("BLOCKED"),
    DELETED("DELETED"),
    PENDING("PENDING"),
    REVIEW("REVIEW");

    private final String value;

    OrganisationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isReview() {
        return this == REVIEW;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}