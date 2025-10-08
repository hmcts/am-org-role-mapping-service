package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTypesResponse {

    // NB: corresponds to CCD-Def-Store:
    //  repository/src/main/java/uk/gov/hmcts/ccd/definition/store/repository/model/AccessTypeJurisdictionResults.java

    private List<AccessTypeJurisdiction> jurisdictions;

}
