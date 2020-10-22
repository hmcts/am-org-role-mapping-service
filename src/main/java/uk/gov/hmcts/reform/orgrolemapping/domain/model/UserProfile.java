package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private String id; // can this be of type UUID?
    private String firstName;
    private String lastName;
    private String emailId;
    private long regionId;
    private String region;
    private String userTypeId;
    private String userType;
    private boolean deleteFlag;
    //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
    private LocalDateTime createdTime;
    //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
    private LocalDateTime lastUpdateTime;
    private List<Role> role;
    public List<BaseLocation> baseLocation;
    private List<WorkArea> workArea;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Role {
        private int roleId;
        private String roleName;
        private boolean primary;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdateTime;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BaseLocation {
        private int locationId;
        private String location;
        private boolean primary;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdateTime;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkArea {
        private int areaOfWork;
        private String serviceCode;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime createdTime;
        //@JsonFormat(pattern = "YYYY-MM-dd HH:mm:ss:SSS")
        private LocalDateTime lastUpdateTime;

    }

}
