package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class FeatureConditionEvaluatorTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    HttpServletRequest request = mock(HttpServletRequest.class);

    @Mock
    HttpServletResponse response = mock(HttpServletResponse.class);

    Map<String, String> launchDarklyMap;

    Object object = new Object();

    @BeforeEach
    public void initializeMocks() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("/welcome", "orm-base-flag");
    }

    @InjectMocks
    FeatureConditionEvaluator featureConditionEvaluator =
            new FeatureConditionEvaluator(ldClient, "dev", "username");

    @ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag"
    })
    void getLaunchDarklyFlagName_Get(String url, String method, String flag)  {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        assertEquals(flag,featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @ParameterizedTest
    @CsvSource({
            "/am/role-mapping/refresh,POST,orm-refresh-role"
    })
    void getLaunchDarklyFlagName_Post(String url, String method, String flag)  {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        assertEquals(flag,featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @ParameterizedTest
    @CsvSource({
            "/welcome,DELETE,orm-base-flag"
    })
    void getLaunchDarklyFlagName_Delete(String url, String method, String flag)  {
        when(request.getMethod()).thenReturn(method);
        assertNull(featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag"
    })
    void getPositiveResponseForFlag(String url, String method, String flag) throws Exception {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        when(ldClient.isFlagKnown(any())).thenReturn(true);
        Assertions.assertTrue(featureConditionEvaluator.preHandle(request, response, object));
    }

    @ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag"
    })
    void getNegativeResponseForFlag(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(false);
        when(ldClient.isFlagKnown(any())).thenReturn(true);
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @ParameterizedTest
    @CsvSource({
            ",GET,orm-base-flag"
    })
    void expectExceptionForNonRegisteredURI(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @ParameterizedTest
    @CsvSource({
            ",POST,orm-base-flag"
    })
    void expectExceptionForRequestMethod(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag"
    })
    void expectExceptionForInvalidFlagName(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        when(ldClient.isFlagKnown(any())).thenReturn(false);
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/url,GET,orm-base-flag"
    })
    void getLdFlagGetCaseNullUrlForbidden(String url, String method, String flag) {
        url = null;
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "/welcome,GET,orm-base-flag",
            "/am/role-mapping/refresh,POST,orm-refresh-role",
    })
    void getLdFlagGetCase(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        String flagName = featureConditionEvaluator.getLaunchDarklyFlag(request);
        Assertions.assertEquals(flag, flagName);
    }

    @ParameterizedTest
    @CsvSource({
            "GET",
            "POST",
    })
    void getLdFlagCase(String method) {
        when(request.getRequestURI()).thenReturn("/am/dummy");
        when(request.getMethod()).thenReturn(method);
        String flagName = featureConditionEvaluator.getLaunchDarklyFlag(request);
        Assertions.assertNull(flagName);
    }

}
