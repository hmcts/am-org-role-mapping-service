package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public  class Appointment implements Serializable {

    @NonNull
    private String roleId;
    private String roleDescEn;
    @NonNull
    private String contractTypeId;
    private String contractTypeDescEn;
    @NonNull
    private String baseLocationId;
    private String courtName;
    private String bench;
    private String courtType;
    private String circuit;
    private String areaOfExpertise;
    @NonNull
    private String locationId;
    private String locationDescEn;
    private String isPrincipalAppointment;
    @NonNull
    private LocalDateTime startDate;
    @NonNull
    private LocalDateTime endDate;
    private String activeFlag;
    private LocalDateTime extractedDate;
    private String appointment;
    private String appointmentType;
    private String serviceCode;
    private String epimmsId;

}
