package uk.gov.hmcts.reform.orgrolemapping.oidc;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.BEARER;

@Component
@Slf4j
public class IdamRepository {

    private IdamApi idamApi;
    private OIdcAdminConfiguration oidcAdminConfiguration;
    private OAuth2Configuration oauth2Configuration;

    @Value("${spring.cache.type}")
    protected String cacheType;

    private CacheManager cacheManager;

    @Autowired
    public IdamRepository(IdamApi idamApi,
                          OIdcAdminConfiguration oidcAdminConfiguration,
                          OAuth2Configuration oauth2Configuration,
                          RestTemplate restTemplate, CacheManager cacheManager) {
        this.idamApi = idamApi;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
        this.oauth2Configuration = oauth2Configuration;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "token")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public UserInfo getUserInfo(String jwtToken) {
        if (cacheType != null && !cacheType.equals("none")) {
            CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("token");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                    .getNativeCache();
            log.debug("generating Bearer Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        return idamApi.retrieveUserInfo(BEARER + jwtToken);
    }

    public UserDetails getUserByUserId(String jwtToken, String userId) {
        return idamApi.getUserByUserId(BEARER + jwtToken, userId);
    }

    @Cacheable(value = "token")
    public String getUserToken() {
        TokenRequest tokenRequest = new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                "password",
                "",
                oidcAdminConfiguration.getUserId(),
                oidcAdminConfiguration.getSecret(),
                oidcAdminConfiguration.getScope(),
                "4",
                ""
        );
        TokenResponse tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }

    @Cacheable(value = "userToken")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 3))
    public String getManageUserToken(String userId) {
        if (cacheType != null && !cacheType.equals("none")) {
            var caffeineCache = (CaffeineCache) cacheManager.getCache("userToken");
            com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = requireNonNull(caffeineCache)
                    .getNativeCache();
            log.debug("Generating system user Token, current size of cache: {}", nativeCache.estimatedSize());
        }
        var tokenRequest = new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                "password",
                "",
                userId,
                oidcAdminConfiguration.getSecret(),
                oidcAdminConfiguration.getScope(),
                "4",
                ""
        );
        var tokenResponse = idamApi.generateOpenIdToken(tokenRequest);
        return tokenResponse.accessToken;
    }
}
