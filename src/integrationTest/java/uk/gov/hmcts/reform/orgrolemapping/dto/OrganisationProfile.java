package uk.gov.hmcts.reform.orgrolemapping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrganisationProfile {

    @JsonProperty("jurisdictions")
    private List<Jurisdiction> jurisdictions;

    @JsonProperty("organisationProfileId")
    private String organisationProfileId;

    public List<Jurisdiction> getJurisdictions() {
        return jurisdictions;
    }

    public void setJurisdictions(List<Jurisdiction> jurisdictions) {
        this.jurisdictions = jurisdictions;
    }

    public String getOrganisationProfileId() {
        return organisationProfileId;
    }

    public void setOrganisationProfileId(String organisationProfileId) {
        this.organisationProfileId = organisationProfileId;
    }
}
