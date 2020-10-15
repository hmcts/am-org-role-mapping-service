package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Validated
@Slf4j
public class RoleAssignmentResource extends RepresentationModel<RoleAssignmentResource> {

    @JsonProperty("roleAssignmentResponse")
    private List<RoleAssignment> roleAssignmentResponse;

}
