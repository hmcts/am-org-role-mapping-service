package uk.gov.hmcts.reform.orgrolemapping.domain.model;

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

    private String organisationIdentifier;
    private String status;
    private LocalDateTime organisationLastUpdated;
    private List<String> organisationProfileIds;
}
