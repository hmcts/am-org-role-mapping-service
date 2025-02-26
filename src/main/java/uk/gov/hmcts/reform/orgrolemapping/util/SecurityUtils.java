package uk.gov.hmcts.reform.orgrolemapping.util;


import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRoles;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.BEARER;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;


@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private final IdamRepository idamRepository;


    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator,
                         JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                         IdamRepository idamRepository) {
        this.authTokenGenerator = authTokenGenerator;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;

        this.idamRepository = idamRepository;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.add("user-id", getUserId());
        headers.add("user-roles", getUserRolesHeader());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        }
        return headers;
    }

    private String getUserBearerToken() {
        return BEARER + getUserToken();
    }


    public String getUserId() {
        if (jwtGrantedAuthoritiesConverter.getUserInfo() != null) {
            return jwtGrantedAuthoritiesConverter.getUserInfo().getUid();
        } else {
            return idamRepository.getUserInfo(getUserToken()).getUid();
        }
    }

    public UserRoles getUserRoles() {
        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        return UserRoles.builder()
                .uid(userInfo.getUid())
                .roles(userInfo.getRoles())
                .build();
    }


    public String getUserToken() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return jwt.getTokenValue();
        } else {
            return idamRepository.getUserToken();
        }
    }

    public String getUserRolesHeader() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }


    public String getServiceName() {
        ServletRequestAttributes servletRequestAttributes =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (servletRequestAttributes != null
                && servletRequestAttributes.getRequest().getHeader(SERVICE_AUTHORIZATION) != null) {
            return JWT.decode(removeBearerFromToken(servletRequestAttributes.getRequest().getHeader(
                    SERVICE_AUTHORIZATION))).getSubject();
        }
        return null;
    }

    private String removeBearerFromToken(String token) {
        if (!token.startsWith(BEARER)) {
            return token;
        } else {
            return token.substring(BEARER.length());
        }
    }

    public String getServiceAuthorizationHeader() {
        return authTokenGenerator.generate();
    }
}