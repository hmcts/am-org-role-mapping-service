package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    @JsonProperty(value = "roleRequest")
    private Request request;
    @JsonProperty(value = "requestedRoles")
    private Collection<RoleAssignment> requestedRoles;
}
