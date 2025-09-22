package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudicialAccessProfile implements Serializable, UserAccessProfile {

    private String userId;
    private String roleId;
    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private List<String> ticketCodes;
    private String regionId;
    private String baseLocationId;
    private String contractTypeId;
    private String appointment;
    private String serviceCode;
    private String appointmentType;
    private String primaryLocationId;
    private List<Authorisation> authorisations;
    private List<String> roles;
    private String status;

    @JsonIgnore
    public boolean isFeePaid() {
        return AppointmentType.isFeePaid(appointmentType);
    }

    @JsonIgnore
    public boolean isSalaried() {
        return AppointmentType.isSalaried(appointmentType);
    }

    @JsonIgnore
    public boolean isVoluntary() {
        return AppointmentType.isVoluntary(appointmentType);
    }

}
