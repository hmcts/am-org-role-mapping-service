package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.IdamFeignClient;

import java.util.List;

@Component
public class IdamFeignClientFallback implements IdamFeignClient {

    @Override
    public ResponseEntity<IdamUser> getUserById(String userId) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamUser> updateUser(String userId, IdamUser user) {
        return ResponseEntity.ok().body(user);
    }

    @Override
    public ResponseEntity<List<IdamInvitation>> getInvitations(String email) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamInvitation> inviteUser(IdamInvitation invitation) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<IdamInvitation> deleteInvitation(String id) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}