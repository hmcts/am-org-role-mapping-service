package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

public enum Status {
    CREATED(10),
    REQUEST_VALIDATED(11),
    REQUEST_NOT_VALIDATED(12),
    ROLE_VALIDATED(13),
    ROLE_NOT_VALIDATED(14),
    APPROVED(15),
    REJECTED(16),
    LIVE(17),
    DELETE_APPROVED(21),
    DELETE_REJECTED(22),
    DELETED(23),


    EXPIRED(41);

    public final Integer sequence;

    Status(Integer sequence) {
        this.sequence = sequence;
    }
}
