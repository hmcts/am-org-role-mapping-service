package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;

import java.time.Instant;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

class SecurityUtilsTest {

    @Mock
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

    @Mock
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            mock(JwtGrantedAuthoritiesConverter.class);

    @Mock
    Authentication authentication = Mockito.mock(Authentication.class);

    @Mock
    SecurityContext securityContext = mock(SecurityContext.class);
    @Mock
    IdamRepository idamRepository = mock(IdamRepository.class);

    @InjectMocks
    private final SecurityUtils securityUtils = new SecurityUtils(
            authTokenGenerator,
            jwtGrantedAuthoritiesConverter,
            idamRepository
    );

    private final String serviceAuthorization = "Bearer eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KA";
    private final String serviceAuthorizationNoBearer = "eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KA";
    private static final String USER_ID = "21334a2b-79ce-44eb-9168-2d49a744be9c";



    private void mockSecurityContextData() {
        List<String> collection = new ArrayList<>();
        collection.add("string");
        Map<String, Object> headers = Map.of("header", "head");
        Jwt jwt = new Jwt(serviceAuthorizationNoBearer, Instant.now(), Instant.now().plusSeconds(10),headers, headers);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);
        when(jwtGrantedAuthoritiesConverter.getUserInfo())
               .thenReturn(TestDataBuilder.buildUserInfo(USER_ID));
        when(authTokenGenerator.generate()).thenReturn(serviceAuthorization);
    }

    @BeforeEach
    public void setUp() {
        mockSecurityContextData();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserId() {
        assertEquals(USER_ID, securityUtils.getUserId());
    }

    @Test
    void getUserRoles() {
        assertNotNull(securityUtils.getUserRoles());
    }

    @Test
    void getUserRolesHeader() {
        assertNotNull(securityUtils.getUserRolesHeader());
    }

    @Test
    void getUserToken() {
        String result = securityUtils.getUserToken();
        assertNotNull(result);
        assertTrue(result.contains("eyJhbG"));
    }

    @Test
    void getUserTokenNoContext() {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(idamRepository.getUserToken()).thenReturn(serviceAuthorization);
        String result = securityUtils.getUserToken();
        assertNotNull(result);
        assertTrue(result.contains("eyJhbG"));
    }

    @Test
    void getServiceAuthorizationHeader() {
        String result = securityUtils.getServiceAuthorizationHeader();
        assertNotNull(result);
        assertTrue(result.contains(serviceAuthorization));
    }

    @Test
    void getAuthorizationHeaders() {
        HttpHeaders result = securityUtils.authorizationHeaders();
        assertEquals(serviceAuthorization, Objects.requireNonNull(result.get(SERVICE_AUTHORIZATION)).get(0));
        assertEquals(USER_ID, Objects.requireNonNull(result.get("user-id")).get(0));
        assertEquals("", Objects.requireNonNull(Objects.requireNonNull(result.get("user-roles")).get(0)));
        assertEquals(serviceAuthorization,
                Objects.requireNonNull(Objects.requireNonNull(result.get("Authorization")).get(0)));
    }

    @Test
    void getAuthorizationHeaders_NoContext() {
        when(securityContext.getAuthentication()).thenReturn(null);
        HttpHeaders result = securityUtils.authorizationHeaders();
        assertEquals(serviceAuthorization, Objects.requireNonNull(result.get(SERVICE_AUTHORIZATION)).get(0));
        assertEquals(USER_ID, Objects.requireNonNull(result.get("user-id")).get(0));
        assertEquals("", Objects.requireNonNull(Objects.requireNonNull(result.get("user-roles")).get(0)));

    }

    @Test
    void removeBearerFromToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SERVICE_AUTHORIZATION, serviceAuthorization);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertEquals("ccd_gw", securityUtils.getServiceName());
    }

    @Test
    void removeBearerFromToken_NoBearerTag() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SERVICE_AUTHORIZATION, serviceAuthorizationNoBearer);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertEquals("ccd_gw", securityUtils.getServiceName());
    }

    @Test
    void shouldNotGetServiceNameFromContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SERVICE_AUTHORIZATION, serviceAuthorizationNoBearer);
        RequestContextHolder.setRequestAttributes(null);
        Assertions.assertNull(securityUtils.getServiceName());
    }

    @Test
    void shouldNotGetServiceNameContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Assertions.assertNull(securityUtils.getServiceName());
    }
}
