package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
// primaryLocationName, areaOfWorkId, serviceCode, deleteFlag) (which will flatten the User Profile into multiple
// userAccessProfile instances based upon roleId X serviceCode).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessProfile {
    private String id;
    private int roleId;
    private String roleName;
    private int primaryLocationId;
    private String primaryLocationName;
    private int areaOfWorkId;
    private String serviceCode;
    private boolean deleteFlag;
}
