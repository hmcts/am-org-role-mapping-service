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
    }

    public static final class Error {
        private Error() {
        }
        public static final String INVALID_REQUEST = "Request is not valid as per validation rule";
        public static final String INVALID_ROLE_NAME = "Invalid role name in the request";
        public static final String BAD_REQUEST_INVALID_PARAMETER = "Invalid Parameter";
        public static final String NO_RECORDS_FOUND_BY_ACTOR = "Role Assignment not found for Actor";
        public static final String NO_RECORDS_FOUND_FOR_CASE_ID = "Role Assignment not found for Case id";
        public static final String INVALID_ROLE_TYPE = "The role type is invalid";
        public static final String INVALID_ACTOR_AND_CASE_ID = "The Actor and Case are empty ";
        public static final String BAD_REQUEST_MISSING_PARAMETERS = "Mandatory Parameters are missing";
        public static final String INVALID_CASE_ID = "The Case id is invalid ";
        public static final String ASSIGNMENT_RECORDS_NOT_FOUND = "No Assignment records found for given criteria";
        public static final String UNPROCESSABLE_ENTITY_REQUEST_REJECTED =
            "Unprocessable entity as request has been rejected";

    }

}
