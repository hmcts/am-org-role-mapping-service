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
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JudicialProfile implements Serializable {

    @NonNull
    private String idamId;
    private String personalCode;
    private String title;
    private String knownAs;
    private String surname;
    private String fullName;
    private String postNominals;

    private String contractTypeId;
    private String workPattern;
    private String emailId;
    private LocalDateTime joiningDate;
    private LocalDateTime lastWorkingDate;
    private String extractedDate;
    private String activeFlag;
    private List<Appointment> appointments;
    private List<Authorisation> authorisations;
    private String userType;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Appointment implements Serializable {

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
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Authorisation implements Serializable {
        @NonNull
        private String authorisationId;
        private String jurisdiction;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime createdDate;
        private LocalDateTime lastUpdatedDate;
        private String lowerLevel;
    }
}
