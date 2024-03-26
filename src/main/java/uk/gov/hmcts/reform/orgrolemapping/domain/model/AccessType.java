package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessType {

    private String organisationProfileId;
    private String accessTypeId;
    private boolean accessMandatory;
    private boolean accessDefault;
    private boolean display;
    private String description;
    private String hint;
    private Integer displayOrder;
    private List<AccessTypeRole> roles;
}
