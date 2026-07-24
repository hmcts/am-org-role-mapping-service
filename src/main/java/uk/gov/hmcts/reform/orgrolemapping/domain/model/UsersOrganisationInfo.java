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

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/
    //                                                                  OrganisationInfoWithUsersResponse.java

    private String organisationIdentifier;
    private String status;
    private List<String> organisationProfileIds;
    private List<ProfessionalUser> users;

}
