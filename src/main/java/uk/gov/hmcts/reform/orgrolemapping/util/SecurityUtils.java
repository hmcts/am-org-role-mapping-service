package uk.gov.hmcts.reform.orgrolemapping.util;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;

@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.SERVICE_AUTHORIZATION2, authTokenGenerator.generate());
        headers.add(HttpHeaders.AUTHORIZATION, getUserAuthorizationHeaders());
        return headers;
    }

    public String getUserAuthorizationHeaders() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            final ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                                                                                                      .getAuthentication()
                                                                                                      .getPrincipal();
            if (serviceAndUser.getPassword() != null) {
                return serviceAndUser.getPassword();
            }
        }
        return null;
    }

    public String getServiceAuthorizationHeader() {
        return authTokenGenerator.generate();
    }

    public String getUserId() {
        final ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                                                                                                  .getAuthentication()
                                                                                                  .getPrincipal();
        return serviceAndUser.getUsername();
    }

    public String getUserToken() {
        final ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                                                                                                  .getAuthentication()
                                                                                                  .getPrincipal();
        return serviceAndUser.getPassword();
    }

    public String getServiceId() {
        final ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                                                                                                  .getAuthentication()
                                                                                                  .getPrincipal();
        return serviceAndUser.getServicename();
    }

    public String getUserRolesHeader() {
        final ServiceAndUserDetails serviceAndUser = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                                                                                                  .getAuthentication()
                                                                                                  .getPrincipal();
        return serviceAndUser.getAuthorities()
                             .stream()
                             .map(GrantedAuthority::getAuthority)
                             .collect(Collectors.joining(","));
    }


}
