package uk.gov.hmcts.reform.orgrolemapping.v1;

public final class V1 {

    private V1() {
    }

    public static final class MediaType {
        private MediaType() {
        }

        // External API
        public static final String SERVICE = "application/vnd.uk.gov.hmcts.am-org-role-mapping-service";
        public static final String MAP_ASSIGNMENTS = SERVICE + ".map-assignments+json;charset=UTF-8;version=1.0";
        public static final String REFRESH_JUDICIAL_ASSIGNMENTS = SERVICE
                + ".map-judicial-assignments+json;charset=UTF-8;version=1.0";
    }

    public static final class Error {
        private Error() {
        }

        public static final String INVALID_REQUEST = "Request is not valid as per validation rule";
        public static final String UNPROCESSABLE_ENTITY_REQUEST_REJECTED =
                "Unprocessable entity as request has been rejected";

    }
}
