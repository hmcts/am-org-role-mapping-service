package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
public class FeatureConditionEvaluatorTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    HttpServletRequest request = mock(HttpServletRequest.class);

    @Mock
    HttpServletResponse response = mock(HttpServletResponse.class);

    Map<String, String> launchDarklyMap;

    Object object = new Object();

    @Before
    public void initializeMocks() {
        launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("/welcome", "orm-base-flag");
    }

    @InjectMocks
    FeatureConditionEvaluator featureConditionEvaluator =
            new FeatureConditionEvaluator(ldClient, "dev", "username");

    @Test
    public void getLaunchDarklyFlagName_Get()  {
        when(request.getRequestURI()).thenReturn("/welcome");
        when(request.getMethod()).thenReturn("GET");
        assertEquals("orm-base-flag",featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @Test
    public void getLaunchDarklyFlagName_Post()  {
        when(request.getRequestURI()).thenReturn("/am/role-mapping/refresh");
        when(request.getMethod()).thenReturn("POST");
        assertEquals("orm-refresh-role",featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @Test
    public void getLaunchDarklyFlagName_Delete()  {
        when(request.getMethod()).thenReturn("DELETE");
        assertNull(featureConditionEvaluator.getLaunchDarklyFlag(request));
    }

    @Test
    public void getPositiveResponseForFlag() throws Exception {
        when(request.getRequestURI()).thenReturn("/welcome");
        when(request.getMethod()).thenReturn("GET");
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        when(ldClient.isFlagKnown(any())).thenReturn(true);
        Assertions.assertTrue(featureConditionEvaluator.preHandle(request, response, object));
    }

    @Test
    public void getNegativeResponseForFlag() {
        when(request.getRequestURI()).thenReturn("/welcome");
        when(request.getMethod()).thenReturn("GET");
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(false);
        when(ldClient.isFlagKnown(any())).thenReturn(true);
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @Test
    public void expectExceptionForNonRegisteredURI() {
        when(request.getRequestURI()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @Test
    public void expectExceptionForRequestMethod() {
        when(request.getRequestURI()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        Assertions.assertThrows(ForbiddenException.class, () ->
                featureConditionEvaluator.preHandle(request, response, object)
        );
    }

    @Test
    public void expectExceptionForInvalidFlagName() {
        when(request.getRequestURI()).thenReturn("/welcome");
        when(request.getMethod()).thenReturn("GET");
        when(ldClient.isFlagKnown(any())).thenReturn(false);
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
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
