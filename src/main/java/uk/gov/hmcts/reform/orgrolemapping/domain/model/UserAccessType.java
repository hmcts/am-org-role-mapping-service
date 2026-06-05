package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessType {

    // NB: corresponds to PRD: src/main/java/uk/gov/hmcts/reform/professionalapi/domain/UserAccessType.java

    private String jurisdictionId;
    private String organisationProfileId;
    private String accessTypeId;
    private Boolean enabled;

}
