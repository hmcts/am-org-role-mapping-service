package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Component
public class FeatureConditionEvaluator implements HandlerInterceptor {

    public static final String USER = "user";
    public static final String SERVICE_NAME = "servicename";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";

    @Autowired
    private LDClientInterface ldClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private final String userName;

    private static final HashMap<String, String> getRequestMap = new HashMap<>();
    private static final HashMap<String, String> postRequestMap = new HashMap<>();

    static {
        //Get Map
        getRequestMap.put("/welcome", "orm-base-flag");
        //post Map
        postRequestMap.put("/am/role-mapping/refresh", "orm-refresh-role");
        getRequestMap.put("/am/role-mapping/fetchFlagStatus","get-db-drools-flag");
        postRequestMap.put("/am/role-mapping/createFeatureFlag","get-db-drools-flag");
        postRequestMap.put("/am/role-mapping/judicial/refresh","orm-judicial-refresh-role-api");
    }

    @Autowired
    public FeatureConditionEvaluator(@Autowired LDClient ldClient,
                                     @Value("${launchdarkly.sdk.environment}") String environment,
                                     @Value("${launchdarkly.sdk.user}") String userName) {
        this.ldClient = ldClient;
        this.environment = environment;
        this.userName = userName;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object arg2) throws Exception {

        var flagName = getLaunchDarklyFlag(request);

        if (flagName == null) {
            throw new ForbiddenException("The endpoint is not configured in Launch Darkly");
        }

        if (!isValidFlag(flagName)) {
            throw new ResourceNotFoundException(String.format(
                    "The flag %s is not configured in Launch Darkly", flagName));
        }

        var flagStatus = isFlagEnabled("am_org_role_mapping_service", flagName);
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
        var uri = request.getRequestURI();
        switch (request.getMethod()) {
            case GET:
                if (getRequestMap.get(uri) != null) {
                    return getRequestMap.get(uri);
                }
                break;
            case POST:
                if (postRequestMap.get(uri) != null) {
                    return postRequestMap.get(uri);
                }
                break;
            default:
        }
        return null;
    }

}
