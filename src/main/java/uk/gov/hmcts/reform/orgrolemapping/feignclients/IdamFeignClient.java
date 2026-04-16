package uk.gov.hmcts.reform.orgrolemapping.feignclients;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.RdFeignClientInterceptor;

import java.util.List;


@FeignClient(value = "idamclient", url = "${idam.api.url}",
        configuration = {FeignClientConfiguration.class, RdFeignClientInterceptor.class},
        fallback = CRDFeignClientFallback.class)

public interface IdamFeignClient {

    @GetMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> getUserById(@PathVariable String userId);

    @PutMapping("/api/v2/users/{userId}")
    ResponseEntity<IdamUser> updateUser(@PathVariable String userId, @Valid @RequestBody IdamUser user);

    @GetMapping("/api/v2/invitations-by-user-email/{email}")
    ResponseEntity<List<IdamInvitation>> getInvitations(@PathVariable String email);

    @PostMapping("/api/v2/invitations")
    ResponseEntity<IdamInvitation> inviteUser(IdamInvitation invitation);

    @DeleteMapping("/api/v2/invitations/{id}")
    ResponseEntity<IdamInvitation> deleteInvitation(@PathVariable String id);
}