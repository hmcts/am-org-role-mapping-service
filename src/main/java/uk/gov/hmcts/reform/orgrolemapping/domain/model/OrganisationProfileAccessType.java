package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationProfileAccessType {
    private String accessTypeId;
    private boolean accessMandatory;
    private boolean accessDefault;
    private Set<AccessTypeRole> roles;
}