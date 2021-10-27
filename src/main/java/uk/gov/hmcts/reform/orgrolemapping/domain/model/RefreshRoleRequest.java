package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRoleRequest {

    @JsonProperty("ccdServiceName")
    private String ccdServiceNames;

    @JsonProperty("object_ids")
    private List<String> objectIds;

    @JsonProperty("sidam_ids")
    private List<String> sidamIds;
}
