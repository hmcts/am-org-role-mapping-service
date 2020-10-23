package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Component
@AllArgsConstructor
public class FeatureConditionEvaluator implements HandlerInterceptor {

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
    private final String environment;

    private static final HashMap<String, String> getRequestMap = new HashMap<>();
    private static final HashMap<String, String> postRequestMap = new HashMap<>();
    private static final HashMap<String, String> deleteRequestMap = new HashMap<>();

    static {
        //Get Map
        getRequestMap.put("/welcome", "orm-base-flag");
    }

    @Value("${launchdarkly.sdk.user}")
    String userName;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object arg2) throws Exception {

        String flagName = getLaunchDarklyFlag(request);

        if (flagName == null) {
            throw new ForbiddenException("The endpoint is not configured in Launch Darkly");
        }

        if (!isValidFlag(flagName)) {
            throw new ResourceNotFoundException(String.format(
                    "The flag %s is not configured in Launch Darkly", flagName));
        }

        boolean flagStatus = isFlagEnabled("am_org_role_mapping_service", flagName);
        if (!flagStatus) {
            throw new ForbiddenException(String.format("Launch Darkly flag is not enabled for the endpoint %s",
                    request.getRequestURI()));
        }
        return flagStatus;
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
