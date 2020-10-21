package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Service
public class FeatureToggleService {

    public static final String USER = "user";
    public static final String SERVICE_NAME = "servicename";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String URI_GET_ASSIGNMENTS_BY_ACTOR_ID = "/am/role-assignments/actors/";
    public static final String LD_FLAG_GET_ROLE_ASSIGNMENTS_BY_ACTOR_ID = "get-role-assignments-by-actor-id";
    public static final String URI_DELETE_ASSIGNMENTS_BY_ID = "/am/role-assignments/";
    public static final String LD_FLAG_DELETE_ROLE_ASSIGNMENTS_BY_ID = "delete-role-assignments-by-id";
    public static final String LD_FLAG_GET_ASSIGNMENTS_BY_QUERY_PARAMS = "get-assignments-by-query-params";

    @Autowired
    private final LDClient ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    private final String userName;
    private static final HashMap<String, String> getRequestMap = new HashMap<>();
    private static final HashMap<String, String> postRequestMap = new HashMap<>();
    private static final HashMap<String, String> deleteRequestMap = new HashMap<>();


    static {
        //Get Map
        getRequestMap.put("/welcome","orm-base-flag");
    }

    @Autowired
    public FeatureToggleService(LDClient ldClient, @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.userName = userName;
    }

    public boolean isFlagEnabled(String serviceName, String flagName) {
        LDUser user = new LDUser.Builder(environment)
                .firstName(userName)
                .lastName(USER)
                .custom(SERVICE_NAME, serviceName)
                .build();

        return ldClient.boolVariation(flagName, user, false);
    }

    public boolean isValidFlag(String flagName) {
        return ldClient.isFlagKnown(flagName);
    }

    public String getLaunchDarklyFlag(HttpServletRequest request) {
        String uri = request.getRequestURI();
        switch (request.getMethod()) {
            case GET:
                if (getRequestMap.get(uri) != null) {
                    return getRequestMap.get(uri);
                }
                break;
            case POST:
            case DELETE:
                break;

            default:
        }
        return null;
    }
}
