package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JudicialProfile implements Serializable {

    private String elinkId;
    private String personalCode;
    private String  title;
    private String  knownAs;
    private String surname;
    private String fullName;
    private String postNominals;
    private String contractTypeId;
    private String workPattern;
    private String  emailId;
    private LocalDateTime joiningDate;
    private LocalDateTime lastWorkingDate;
    private String extractedDate;
    private String  activeFlag;
    private List<Appointment> appointments;
    private List<Authorisation>  authorisations;
    private String userType;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class  Appointment implements Serializable {

        private String roleId;
        private String oleDesc_En;
        private String contractTypeId;
        private String contractTypeDescEn;
        private String baseLocationId;
        private String courtName;
        private String bench;
        private String courtType;
        private String circuit;
        private String areaOfExpertise;
        private String locationId;
        private String locationDescEn;
        private String isPrincipalAppointment;
        private LocalDateTime  startDate;
        private LocalDateTime endDate;
        private String activeFlag;
        private LocalDateTime extractedDate;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class  Authorisation implements Serializable {
        private String authorisationId ;
        private String jurisdiction ;
        private LocalDateTime startDate ;
        private LocalDateTime endDate ;
        private LocalDateTime createdDate ;
        private LocalDateTime lastUpdatedDate ;
        private String lowerLevel;
    }
}
