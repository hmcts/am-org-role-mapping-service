package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JRDUserRequest {

    @JsonProperty("ccdServiceName")
    private String ccdServiceNames;

    @JsonProperty("object_ids")
    private Set<String> objectIds;

    @JsonProperty("sidam_ids")
    private Set<String> sidamIds;

    @JsonProperty("personal_code")
    private Set<String> personalCode;

}
