package uk.gov.hmcts.reform.orgrolemapping.util;

import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;

import static org.mockito.Mockito.mock;

class SecurityUtilsTest {

    @Mock
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            mock(JwtGrantedAuthoritiesConverter.class);

    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final SecurityUtils securityUtils = new SecurityUtils(authTokenGenerator, jwtGrantedAuthoritiesConverter);
    private final String serviceAuthorization = "auth";
    private static final String USER_ID = "userId";

    /* @Test
    void shouldGetAuthorizationHeaders() {
        mockSecurityContextData();

        HttpHeaders httpHeaders = securityUtils.authorizationHeaders();
        assertEquals(2, httpHeaders.size());
    }*/

    /*private void mockSecurityContextData() {
        List<String> collection = new ArrayList<String>();
        collection.add("string");
        ServiceAndUserDetails serviceAndUserDetails = new ServiceAndUserDetails(
            USER_ID,
            serviceAuthorization,
            collection,
            "servicename"
        );
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);
    }

    @Test
    void getUserId() {
        mockSecurityContextData();
        assertEquals("userId", securityUtils.getUserId());
    }

    @Test
    void getUserToken() {
        mockSecurityContextData();
        assertEquals("auth", securityUtils.getUserToken());
    }

    @Test
    void getUserRolesHeader() {
        mockSecurityContextData();
        assertEquals("string", securityUtils.getUserRolesHeader());
    }*/

    /*@Test
    void getServiceId() {
        mockSecurityContextData();
        assertEquals("servicename", securityUtils.getServiceName());
    }*/

}
