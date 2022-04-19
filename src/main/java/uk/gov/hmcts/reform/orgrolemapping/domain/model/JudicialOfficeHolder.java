package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JudicialOfficeHolder implements Serializable {

    private String userId; //Idam Id
    private String office; // the business role
    private String jurisdiction; // jurisdiction in CCD like IA
    private List<String> ticketCodes; // Ticket code list from JRD
    private ZonedDateTime beginTime; //from appointment data
    private ZonedDateTime endTime; //from appointment data and add +1 day as per mapping rule by Jon
    private String regionId; // locationId from appointment
    private String  baseLocationId; // epims id
    private String primaryLocation; //epims id where isPrimary is true
    private String  contractType; //appointment type from JRD
}