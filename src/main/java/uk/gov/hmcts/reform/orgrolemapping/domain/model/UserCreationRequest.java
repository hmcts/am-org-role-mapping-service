package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationRequest {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/controller/request/
    //                                                                   UserCreationRequest.java

    private String firstName;

    private String lastName;

    private String email;
}