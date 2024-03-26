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
    private String organisationIdentifier;
    private OrganisationStatus status;
    @JsonProperty("lastUpdated")
    private LocalDateTime organisationLastUpdated;
    private List<String> organisationProfileIds;
}
