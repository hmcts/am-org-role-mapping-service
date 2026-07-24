package uk.gov.hmcts.reform.orgrolemapping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AccessTypeString {

    @JsonProperty("roles")
    private List<Role> roles;

    @JsonProperty("accessTypeId")
    private String accessTypeId;

    @JsonProperty("accessDefault")
    private boolean accessDefault;

    @JsonProperty("accessMandatory")
    private boolean accessMandatory;

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getAccessTypeId() {
        return accessTypeId;
    }

    public void setAccessTypeId(String accessTypeId) {
        this.accessTypeId = accessTypeId;
    }

    public boolean isAccessDefault() {
        return accessDefault;
    }

    public void setAccessDefault(boolean accessDefault) {
        this.accessDefault = accessDefault;
    }

    public boolean isAccessMandatory() {
        return accessMandatory;
    }

    public void setAccessMandatory(boolean accessMandatory) {
        this.accessMandatory = accessMandatory;
    }
}
