package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RoleV2 implements Serializable {

    private String judiciaryRoleName;
    private String judiciaryRoleId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
