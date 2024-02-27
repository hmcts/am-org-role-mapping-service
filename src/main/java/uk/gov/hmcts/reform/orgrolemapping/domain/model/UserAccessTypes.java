package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAccessTypes {

    private String jurisdictionId;
    private String organisationProfileId;
    private String accessTypeId;
    private String enabled;

    //        [
//        {
//        "jurisdictionId": "12345",
//        "organisationProfileId": "SOLICITOR_PROFILE",
//        "accessTypeId": "1234",
//        "enabled": "true"
//        }
//        ]
}
