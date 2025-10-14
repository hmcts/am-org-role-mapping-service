package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationCreationRequest {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/request/
    //                                                                   OrganisationCreationRequest.java

    private String name;

    private String status;

    private String statusMessage;

    private String sraId;

    private String sraRegulated;

    private String companyNumber;

    private String companyUrl;

    private UserCreationRequest superUser;

    private Set<String> paymentAccount;

    private List<ContactInformationCreationRequest> contactInformation;
}
