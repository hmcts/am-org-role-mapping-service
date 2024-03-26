package uk.gov.hmcts.reform.orgrolemapping.domain.model.constants;

import java.time.format.DateTimeFormatter;

public class PrmConstants {

    public static final String ORGANISATION_ID = "organisationId";
    public static final String ORGANISATION_LAST_UPDATED = "organisationLastUpdated";
    public static final String ACCESS_TYPES_MIN_VERSION = "accessTypesMinVersion";
    public static final String SINCE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(SINCE_TIMESTAMP_FORMAT);
}
