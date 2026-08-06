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
public class AccessTypeJurisdiction {

    // NB: corresponds to CCD-Def-Store:
    //  repository/src/main/java/uk/gov/hmcts/ccd/definition/store/repository/model/AccessTypeJurisdictionResult.java


    private String jurisdictionId;
    private String jurisdictionName;
    private List<AccessType> accessTypes;

}
