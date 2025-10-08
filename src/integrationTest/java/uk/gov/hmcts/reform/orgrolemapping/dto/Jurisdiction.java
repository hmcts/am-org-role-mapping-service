package uk.gov.hmcts.reform.orgrolemapping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Jurisdiction {

    @JsonProperty("accessTypes")
    private List<AccessTypeString> accessTypes;

    @JsonProperty("jurisdictionId")
    private String jurisdictionId;

    public List<AccessTypeString> getAccessTypes() {
        return accessTypes;
    }

    public void setAccessTypes(List<AccessTypeString> accessTypes) {
        this.accessTypes = accessTypes;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }
}
