package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationInfo {

    // NB: corresponds to PRD:
    //   * src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/OrganisationByProfileResponse.java
    // and
    //   * src/main/java/uk/gov/hmcts/reform/professionalapi/domain/OrganisationInfo.java
    // and also part of
    //   src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/OrganisationsDetailResponse.java

    private String organisationIdentifier;
    private String status;
    @JsonProperty("lastUpdated")
    private LocalDateTime organisationLastUpdated;
    private List<String> organisationProfileIds;

}
