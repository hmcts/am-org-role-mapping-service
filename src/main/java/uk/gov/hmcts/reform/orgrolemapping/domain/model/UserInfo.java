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
public class UserInfo {

    private String userId;
    private LocalDateTime lastUpdated;
    private List<String> userProfileIds;
    private String deleted;
    private String organisationId;
    private String organisationStatus;
    private String organisationProfileIds;
    private String accessTypes;
    private boolean active;

}
