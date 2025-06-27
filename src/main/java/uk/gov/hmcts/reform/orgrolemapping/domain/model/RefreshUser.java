package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class RefreshUser {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/domain/RefreshUser.java

    private String userIdentifier;
    private LocalDateTime lastUpdated;
    private OrganisationInfo organisationInfo;
    private List<UserAccessType> userAccessTypes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime dateTimeDeleted;

}
