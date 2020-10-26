package uk.gov.hmcts.reform.orgrolemapping.config;


import feign.FeignException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityEndpointFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof FeignException.FeignClientException) {
                FeignException.FeignClientException feignClientException =
                        (FeignException.FeignClientException) throwable;
                response.setStatus(feignClientException.status());
                return;
            }
            throw e;
        }
    }
}