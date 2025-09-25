package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.jrd.AppointmentEnum;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.validateAuthorisation;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudicialAccessProfile implements Serializable, UserAccessProfile {

    private String userId;
    private String roleId; // appointment code
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

    @JsonIgnore
    public boolean hasAppointmentCode(AppointmentEnum appointment) {
        return roleId != null && appointment.getCodes().contains(roleId);
    }

    @JsonIgnore
    public boolean hasValidAuthorisation(Jurisdiction jurisdiction) {
        return jurisdiction.getServiceCodes().stream()
            .anyMatch(testServiceCode -> validateAuthorisation(authorisations, testServiceCode));
    }

    @JsonIgnore
    public boolean hasValidEndDate() {
        return (endTime == null || endTime.compareTo(ZonedDateTime.now()) >= 0);
    }

}
