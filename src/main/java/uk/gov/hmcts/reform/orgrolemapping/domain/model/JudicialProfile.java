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
    private String sidamId;
    private String objectId;
    private String knownAs;
    private String surname;
    private String fullName;
    private String postNominals;
    private String emailId;
    private List<Appointment> appointments;
    private List<Authorisation> authorisations;
 }
