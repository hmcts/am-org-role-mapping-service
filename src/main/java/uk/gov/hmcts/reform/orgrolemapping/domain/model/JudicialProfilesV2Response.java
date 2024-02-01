package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class JudicialProfilesV2Response implements Serializable {
    @JsonProperty(value = "ccd_service_name")
    private String serviceName;
    @JsonProperty(value = "judicial_profile")
    private JudicialProfileV2 judicialProfile;
}