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
public class OrganisationByProfileIdsResponse {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/
    //                                                                    MultipleOrganisationsResponse.java

    private List<OrganisationInfo> organisationInfo;
    private String lastRecordInPage;
    private Boolean moreAvailable;

}
