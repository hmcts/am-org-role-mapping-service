package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudicialAccessProfile implements Serializable {

   private String userId;
   private String roleId;
    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private List<String> authorisations;
    private String regionId;
    private String  baseLocationId;
    private String  contractTypeId;

}
