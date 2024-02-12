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
public class UsersByOrganisationResponse {

    private List<OrganisationInfo> organisationInfo;
    private String lastOrgInPage;
    private String lastUserInPage;
    private Boolean moreAvailable;
}
