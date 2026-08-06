package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRefreshQueueValue {

    private String organisationProfileId;

    @Schema(example = "1")
    private int accessTypesMinVersion;

    @Schema(example = "true")
    private boolean active;

}
