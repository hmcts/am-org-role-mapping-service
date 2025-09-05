package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationInfo {

    // NB: corresponds to PRD:
    //   * src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/OrganisationByProfileResponse.java
    // and
    //   * src/main/java/uk/gov/hmcts/reform/professionalapi/domain/OrganisationInfo.java
    // and also part of
    //   src/main/java/uk/gov/hmcts/reform/professionalapi/controller/response/OrganisationsDetailResponse.java

    private String organisationIdentifier;
    private OrganisationStatus status;
    @JsonProperty("lastUpdated")
    private LocalDateTime organisationLastUpdated;
    private List<String> organisationProfileIds;

}
