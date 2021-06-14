package uk.gov.hmcts.reform.orgrolemapping.apihelper;

public class Constants {

    private Constants() {
    }

    public static final String BAD_REQUEST = "Bad Request";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String FORBIDDEN = "Forbidden: Insufficient permissions";
    public static final String APPLICATION_JSON = "application/json";
    public static final String S2S_API_PARAM = "Service Auth (S2S). Use it when accessing the API on App Tier level.";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String BEARER = "Bearer ";
    public static final String NUMBER_TEXT_HYPHEN_PATTERN = "^[-a-zA-Z0-9]*$";

    public static final String INPUT_CASE_ID_PATTERN = "^[0-9]*$";
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm";
    public static final String PROD = "PROD";
    public static final String NUMBER_PATTERN = "^[0-9]{16}$";
    public static final String NUMBER_TEXT_PATTERN = "^[a-zA-Z0-9]+$";
    public static final String TEXT_HYPHEN_PATTERN = "^[-a-zA-Z]*$";
    public static final String TEXT_PATTERN = "^[a-zA-Z]*$";
    public static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-"
            + "[089ab][0-9a-f]{3}-[0-9a-f]{12}$";
    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";

    public static final String ROLES_JSON = "role.json";
    public static final String ROLE_JSON_PATTERNS_FIELD = "patterns";

    public static final String ROLETYPE = "RoleType";
    public static final String CLASSIFICATION = "Classification";
    public static final String UUID = "UUID";
    public static final String ACTORIDTYPE = "ActorIdType";
    public static final String GRANTTYPE = "GrantType";
    public static final String ROLECATEGORY = "RoleCategory";
    public static final String BOOLEAN = "Boolean";
    public static final String LOCALDATETIME = "LocalDateTime";
    public static final String STATUS = "Status";
    public static final String INTEGER = "Integer";
    public static final String REQUEST_BODY = "Request body";
    public static final String SERVICE_AUTHORIZATION2 = "ServiceAuthorization";
    public static final String ASB_PUBLISH_ERROR = "There was an error in communication with Azure Service Bus."
            + " Please try again later.";
    public static final String ABORTED = "ABORTED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED_JOB = "Job is failed for userIds ::  %s ";
    public static final String SUCCESS_JOB = "Job is success for userIds ::  %s ";

}
