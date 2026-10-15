package uk.gov.hmcts.reform.orgrolemapping.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class IdamRepositoryTest {

    @Mock
    private IdamApi idamApi = mock(IdamApi.class);

    @Mock
    private OIdcAdminConfiguration oidcAdminConfiguration = mock(OIdcAdminConfiguration.class);

    @Mock
    private OAuth2Configuration oauth2Configuration = mock(OAuth2Configuration.class);

    @Mock
    private CacheManager cacheManager = mock(CacheManager.class);

    private RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    IdamRepository idamRepository;

    String userId = "abcd";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        idamRepository = new IdamRepository(idamApi, oidcAdminConfiguration,
                oauth2Configuration, restTemplate, cacheManager
        );
        ReflectionTestUtils.setField(
                idamRepository,
                "cacheType", "null"

        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(cacheManager, times(1)).getCache(any());
        verify(caffeineCacheMock, times(1)).getNativeCache();
        verify(cache, times(1)).estimatedSize();

    }

    @Test
    void getUserRolesBlankResponse() {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";
        UserDetails userDetails = UserDetails.builder().email("black@betty.com").forename("ram").surname("jam").id(
                "1234567890123456")
                .roles(null).build();

        when(idamApi.getUserByUserId(any(), any())).thenReturn(userDetails);

        assertNotNull(idamRepository.getUserByUserId("Test", userId));
    }

    @Test
    void getUserToken() {

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getUserId()).thenReturn("userid");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
                TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getUserToken();

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getManageUserToken() {
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(1L);

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
                TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
    }

    @Test
    void getManageUserToken_cacheNone() {

        ReflectionTestUtils.setField(
                idamRepository,
                "cacheType", "none"

        );

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
                TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();
    }

    @Test
    void getManageUserToken_cacheNull() {

        ReflectionTestUtils.setField(
                idamRepository,
                "cacheType", null
        );

        when(oauth2Configuration.getClientId()).thenReturn("clientId");
        when(oauth2Configuration.getClientSecret()).thenReturn("secret");
        when(oidcAdminConfiguration.getSecret()).thenReturn("password");
        when(oidcAdminConfiguration.getScope()).thenReturn("scope");
        TokenResponse tokenResponse = new
                TokenResponse("a", "1", "1", "a", "v", "v");
        when(idamApi.generateOpenIdToken(any())).thenReturn(tokenResponse);

        String result = idamRepository.getManageUserToken("123");

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertFalse(result.isEmpty());
        verify(cacheManager, times(0)).getCache(any());

        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserInfo_CacheTypeNone() {

        ReflectionTestUtils.setField(
                idamRepository,
                "cacheType", "none"

        );

        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(cacheManager, times(0)).getCache(any());
        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();

    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserInfo_CacheTypeNull() {

        ReflectionTestUtils.setField(
                idamRepository,
                "cacheType", null

        );

        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        var cache = mock(com.github.benmanes.caffeine.cache.Cache.class);

        when(idamApi.retrieveUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
        verify(idamApi, times(1)).retrieveUserInfo(any());
        verify(caffeineCacheMock, times(0)).getNativeCache();
        verify(cache, times(0)).estimatedSize();
        verify(cacheManager, times(0)).getCache(any());
    }
}
