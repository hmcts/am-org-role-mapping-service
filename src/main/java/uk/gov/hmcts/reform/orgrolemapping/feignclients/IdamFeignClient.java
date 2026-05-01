package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import jakarta.validation.Valid;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.IdamFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.IdamFeignClientInterceptor;

import java.util.List;


@FeignClient(value = "idamclient", url = "${feign.client.config.idamClient.url}",
        configuration = {FeignClientConfiguration.class, IdamFeignClientInterceptor.class},
        fallback = IdamFeignClientFallback.class)

public interface IdamFeignClient {

    @GetMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> getUserById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String userId);

    @GetMapping("/api/v2/users-by-email/{email}")
    ResponseEntity<IdamUser> getUserByEmail(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String email);

    @PutMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> updateUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String userId, @Valid @RequestBody IdamUser user);

    @GetMapping("/api/v2/invitations-by-user-email/{email}")
    ResponseEntity<List<IdamInvitation>> getInvitations(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String email);

    @PostMapping("/api/v2/invitations")
    ResponseEntity<IdamInvitation> inviteUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            IdamInvitation invitation);

    @DeleteMapping("/api/v2/invitations/{id}")
    ResponseEntity<IdamInvitation> deleteInvitation(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable String id);

    @PostMapping("/o/token")
    TokenResponse getToken(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("grant_type") String grantType,
            @RequestParam("scope") String scope
    );
}