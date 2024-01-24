package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


//b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
// primaryLocationName, areaOfWorkId, serviceCode, suspended) (which will flatten the User Profile into multiple
// userAccessProfile instances based upon roleId X serviceCode).


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Jurisdiction implements Serializable {

    private String jurisdictionName;
    private List<OrganisationProfileAccessType> accessTypes;
}
