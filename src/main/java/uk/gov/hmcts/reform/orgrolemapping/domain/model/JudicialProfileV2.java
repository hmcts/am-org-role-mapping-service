package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JudicialProfileV2 implements Serializable, UserAccessProfile {

    private String sidamId;
    private String objectId;
    private String knownAs;
    private String surname;
    private String fullName;
    private String postNominals;
    private String emailId;
    private String personalCode;
    private String title;
    private String initials;
    private String retirementDate;
    private String activeFlag;
    private String deletedFlag;
    private List<AppointmentV2> appointments;
    private List<AuthorisationV2> authorisations;
    private List<RoleV2> roles;
}
