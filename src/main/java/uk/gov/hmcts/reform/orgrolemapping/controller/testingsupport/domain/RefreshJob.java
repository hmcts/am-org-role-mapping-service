package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshJob implements Serializable {

    @Schema(example = "1")
    private Long jobId;

    private String roleCategory;

    private String jurisdiction;

    private String status;

    private String comments;

    private String[] userIds;

    private String log;

    @Schema(example = "1")
    private Long linkedJobId;

    @Schema(example = "2024-04-26T09:06:34.417Z")
    private ZonedDateTime created;
}
