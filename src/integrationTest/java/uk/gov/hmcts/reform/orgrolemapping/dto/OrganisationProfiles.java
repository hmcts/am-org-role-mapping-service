package uk.gov.hmcts.reform.orgrolemapping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrganisationProfiles {

    @JsonProperty("organisationProfiles")
    private List<OrganisationProfile> organisationProfiles;

    public List<OrganisationProfile> getOrganisationProfiles() {
        return organisationProfiles;
    }

    public void setOrganisationProfiles(List<OrganisationProfile> organisationProfiles) {
        this.organisationProfiles = organisationProfiles;
    }
}
