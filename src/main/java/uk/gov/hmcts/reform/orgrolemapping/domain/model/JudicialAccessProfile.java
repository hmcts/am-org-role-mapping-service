package uk.gov.hmcts.reform.orgrolemapping.domain.model;

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
public class JudicialAccessProfile implements Serializable, UserAccessProfile {

    private String userId;
    private String roleId;
    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private List<String> ticketCodes;
    private String regionId;
    /**
     * @deprecated This field is deprecated & may be removed after AM/RD v2 go live. Use regionId instead. See COT-546
     */
    @Deprecated(forRemoval = true)
    private String cftRegionIdV1;
    private String baseLocationId;
    private String contractTypeId;
    private String appointment;
    private String serviceCode;
    private String appointmentType;
    private String primaryLocationId;
    private List<Authorisation> authorisations;
    private List<String> roles;
    private String status;
}
