package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteOrganisationResponse {

    // NB: corresponds to PRD:
    //   * src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/DeleteOrganisationResponse.java

    private int statusCode;

    private String message;
}
