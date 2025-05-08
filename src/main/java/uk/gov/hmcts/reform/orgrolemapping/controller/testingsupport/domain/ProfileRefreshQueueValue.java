package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "An entry from the PRM Profile Refresh Queue",
    example = """
     {
         "organisationProfileId": "SOLICITOR_PROFILE",
         "accessTypesMinVersion": 1,
         "active": true
     }
     """
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRefreshQueueValue {

    private String organisationProfileId;

    private int accessTypesMinVersion;

    private boolean active;

}
