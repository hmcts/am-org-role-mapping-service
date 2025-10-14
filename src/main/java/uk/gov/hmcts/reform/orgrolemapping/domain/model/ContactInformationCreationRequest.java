package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactInformationCreationRequest {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/request/
    //                                                                   ContactInformationCreationRequest.java

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String townCity;

    private String county;

    private String country;

    private String postCode;
}
