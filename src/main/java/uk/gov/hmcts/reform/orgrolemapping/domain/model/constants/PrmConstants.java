package uk.gov.hmcts.reform.orgrolemapping.domain.model.constants;

import java.time.format.DateTimeFormatter;

public class PrmConstants {

    public static final String ORGANISATION_ID = "organisationId";
    public static final String ORGANISATION_LAST_UPDATED = "organisationLastUpdated";
    public static final String ACCESS_TYPES_MIN_VERSION = "accessTypesMinVersion";
    public static final String SINCE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern(SINCE_TIMESTAMP_FORMAT);
    public static final String ORGANISATION_STATUS = "organisationStatus";
    public static final String ORGANISATION_PROFILE_IDS = "organisationProfileIds";
    public static final String USER_LAST_UPDATED = "userLastUpdated";
    public static final String ACCESS_TYPES = "accessTypes";
    public static final String USER_ID = "userId";
    public static final String DELETED = "deleted";

}
