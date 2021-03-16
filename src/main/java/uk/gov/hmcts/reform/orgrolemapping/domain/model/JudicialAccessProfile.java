package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
public class JudicialAccessProfile implements Serializable {

   private String roleId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private String   authorisations;
    private String regionId;
    private String  locationId;
    private String  contractTypeId;

}
