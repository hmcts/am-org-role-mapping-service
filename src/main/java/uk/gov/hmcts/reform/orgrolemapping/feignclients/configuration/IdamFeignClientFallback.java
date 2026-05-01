package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.IdamFeignClient;

import java.util.List;

@Component
public class IdamFeignClientFallback implements IdamFeignClient {

    @Override
    public ResponseEntity<IdamUser> getUserById(String idamToken, String userId) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamUser> getUserByEmail(String idamToken, String email) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamUser> updateUser(String idamToken, String userId, IdamUser user) {
        return ResponseEntity.ok().body(user);
    }

    @Override
    public ResponseEntity<List<IdamInvitation>> getInvitations(String idamToken, String email) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamInvitation> inviteUser(String idamToken, IdamInvitation invitation) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamInvitation> deleteInvitation(String idamToken, String id) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public TokenResponse getToken(
            String clientId, String clientSecret, String redirectUri, String grantType, String scope) {
        return null;
    }
}