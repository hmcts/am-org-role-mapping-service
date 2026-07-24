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
public class OrganisationsResponse {

    // NB: corresponds to PRD:
    //        src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/OrganisationsDetailResponse.java

    private List<OrganisationInfo> organisations;
    private Boolean moreAvailable;

}
