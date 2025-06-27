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

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/
    //                                              UsersInOrganisationsByOrganisationIdentifiersResponse.java

    private List<UsersOrganisationInfo> organisationInfo;
    private String lastOrgInPage;
    private String lastUserInPage;
    private Boolean moreAvailable;

}
