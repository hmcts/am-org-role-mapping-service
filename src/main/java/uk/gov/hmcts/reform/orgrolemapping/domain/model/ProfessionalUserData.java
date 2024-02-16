package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfessionalUserData {

    private String userId;
    private LocalDateTime lastUpdated;
    private LocalDateTime deleted;
    private String accessTypes;
    private String organisationId;
    private String organisationStatus;
    private String organisationProfileIds;
}