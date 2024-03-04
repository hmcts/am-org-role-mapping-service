package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationInfo {
    private String organisationIdentifier;
    private OrganisationStatus status;
    private LocalDateTime lastUpdated;
    private List<String> organisationProfileIds;
}
