package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationRefreshQueueValue {

    private String organisationId;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime organisationLastUpdated;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime lastUpdated;

    @Schema(example = "1")
    private int accessTypesMinVersion;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "1")
    private int retry;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime retryAfter;

}
