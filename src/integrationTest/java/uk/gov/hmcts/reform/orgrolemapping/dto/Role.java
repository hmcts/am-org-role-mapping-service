package uk.gov.hmcts.reform.orgrolemapping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Role {
    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("groupRoleName")
    private String groupRoleName;

    @JsonProperty("groupAccessEnabled")
    private boolean groupAccessEnabled;

    @JsonProperty("caseGroupIdTemplate")
    private String caseGroupIdTemplate;

    @JsonProperty("organisationalRoleName")
    private String organisationalRoleName;

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public String getGroupRoleName() {
        return groupRoleName;
    }

    public void setGroupRoleName(String groupRoleName) {
        this.groupRoleName = groupRoleName;
    }

    public boolean isGroupAccessEnabled() {
        return groupAccessEnabled;
    }

    public void setGroupAccessEnabled(boolean groupAccessEnabled) {
        this.groupAccessEnabled = groupAccessEnabled;
    }

    public String getCaseGroupIdTemplate() {
        return caseGroupIdTemplate;
    }

    public void setCaseGroupIdTemplate(String caseGroupIdTemplate) {
        this.caseGroupIdTemplate = caseGroupIdTemplate;
    }

    public String getOrganisationalRoleName() {
        return organisationalRoleName;
    }

    public void setOrganisationalRoleName(String organisationalRoleName) {
        this.organisationalRoleName = organisationalRoleName;
    }
}
