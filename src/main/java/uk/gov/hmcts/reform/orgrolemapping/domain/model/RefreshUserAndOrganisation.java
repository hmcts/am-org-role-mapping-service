package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshUserAndOrganisation {

    private String userIdentifier;
    private LocalDateTime userLastUpdated;
    private String organisationIdentifier;
    private String organisationStatus;
    private String organisationProfileIds;
    private String userAccessTypes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime dateTimeDeleted;
}
