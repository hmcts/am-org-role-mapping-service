package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
// primaryLocationName, areaOfWorkId, serviceCode, suspended) (which will flatten the User Profile into multiple
// userAccessProfile instances based upon roleId X serviceCode).


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessProfile implements Serializable {
    private String id;
    private String roleId;
    private String roleName;
    private String primaryLocationId;
    private String primaryLocationName;
    private String areaOfWorkId;
    private String serviceCode;
    private boolean suspended;


}
