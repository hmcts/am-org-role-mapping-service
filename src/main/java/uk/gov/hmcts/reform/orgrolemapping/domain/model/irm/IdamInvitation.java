package uk.gov.hmcts.reform.orgrolemapping.domain.model.irm;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.InvitationStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.irm.InvitationType;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdamInvitation {

    private String id;

    private InvitationType invitationType;
    private InvitationStatus invitationStatus;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String activationToken;

    private String userId;

    @NotEmpty
    private String email;

    private String forename;
    private String surname;
    private List<String> activationRoleNames;
    private String clientId;
    private String successRedirect;
    private String invitedBy;
    private ZonedDateTime createDate;
    private ZonedDateTime lastModified;

}
