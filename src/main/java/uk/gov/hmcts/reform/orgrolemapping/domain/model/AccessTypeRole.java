package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccessTypeRole {

    private String caseTypeId;
    private String organisationalRoleName;
    private String groupRoleName;
    private String caseGroupIdTemplate;
    private boolean groupAccessEnabled;
}
