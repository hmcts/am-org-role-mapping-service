package uk.gov.hmcts.reform.orgrolemapping.apihelper;

public class Constants {

    private Constants() {
    }

    public static final String BAD_REQUEST = "Bad Request";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String FORBIDDEN = "Forbidden: Insufficient permissions";
    public static final String APPLICATION_JSON = "application/json";
    public static final String SERVICE_AUTHORIZATION = "serviceauthorization";
    public static final String S2S_API_PARAM = "Service Auth (S2S). Use it when accessing the API on App Tier level.";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION2 = "ServiceAuthorization";
    public static final String BEARER = "Bearer ";

    public static final String INPUT_CASE_ID_PATTERN = "^[0-9]*$";

}
