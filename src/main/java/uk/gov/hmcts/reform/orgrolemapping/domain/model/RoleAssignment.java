package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Classification;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignment {

    private ActorIdType actorIdType; // will be set to IDAM
    private String actorId; // will be set as per User Id
    private RoleType roleType; // will be set to ORGANISATIONAL for case-worker
    private String roleName; // will be set as per mapping rule
    private Classification classification; // will be set to PUBLIC for case-worker
    private GrantType grantType; // will be set to ORGANISATION for case-worker
    private RoleCategory roleCategory; // will be set to STAFF for case-worker
    private boolean readOnly; // will be set to false for case-worker
    private LocalDateTime beginTime; // will be set to null for case-worker
    private LocalDateTime endTime; // will be set to null for case-worker
    // there are only 2 attributes identified 1)jurisdiction=IA and primaryLocation=<Extract from Staff user>
    private Map<String, JsonNode> attributes;
    private JsonNode notes; //this would be empty for case-worker and reserved for future requirements.
    private List<String> authorisations; // this is not applicable for case-worker

}
