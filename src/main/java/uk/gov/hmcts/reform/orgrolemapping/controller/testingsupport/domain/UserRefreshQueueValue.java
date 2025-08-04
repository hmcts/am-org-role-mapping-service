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
public class UserRefreshQueueValue {

    private String userId;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime userLastUpdated;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime lastUpdated;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime deleted;

    @Schema(example = "A")
    private String organisationId;

    @Schema(example = "ACTIVE")
    private String organisationStatus;

    @Schema(example = "{SOLICITOR_PROFILE}")
    private String[] organisationProfileIds;

    private String accessTypes;

    @Schema(example = "1")
    private int accessTypesMinVersion;

    @Schema(example = "true")
    private boolean active;

    @Schema(example = "1")
    private int retry;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private LocalDateTime retryAfter;

}
