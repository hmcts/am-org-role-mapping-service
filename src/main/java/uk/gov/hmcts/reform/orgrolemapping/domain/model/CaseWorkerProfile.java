package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CaseWorkerProfile  implements Serializable  {
    private String id; // can this be of type UUID?
    private String firstName;
    private String lastName;
    private String emailId;
    private long regionId;
    private String region;
    private String userTypeId;
    private String userType;
    private boolean suspended;
    private String caseAllocator;
    private String taskSupervisor;
    //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
    private LocalDateTime createdTime;
    //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
    private LocalDateTime lastUpdatedTime;
    private List<Role> role;
    private List<BaseLocation> baseLocation;
    private List<WorkArea> workArea;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Role implements Serializable {
        private String roleId;
        @JsonProperty("role")
        private String roleName;
        @JsonProperty("is_primary")
        private boolean primary;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdatedTime;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class BaseLocation implements Serializable {
        private String locationId;
        private String location;
        @JsonProperty("is_primary")
        private boolean primary;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdatedTime;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class WorkArea implements Serializable {
        private String areaOfWork;
        private String serviceCode;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdatedTime;

    }

}