package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsersOrganisationInfo {

    private String organisationIdentifier;
    private String status;
    private List<String> organisationProfileIds;
    private List<ProfessionalUser> users;
}
