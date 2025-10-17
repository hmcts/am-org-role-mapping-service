package uk.gov.hmcts.reform.orgrolemapping.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Getter
@Named
@Singleton
public class ProfessionalUserServiceConfig {

    private final String orgRetryOneIntervalMin;
    
    private final String orgRetryTwoIntervalMin;
    
    private final String orgRetryThreeIntervalMin;

    private final String userRetryOneIntervalMin;

    private final String userRetryTwoIntervalMin;

    private final String userRetryThreeIntervalMin;

    private final String activeUserRefreshDays;

    private final String pageSize;

    private final String tolerance;

    private final boolean refreshApiEnabled;

    public ProfessionalUserServiceConfig(
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryOneIntervalMin}")
            String orgRetryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryTwoIntervalMin}")
            String orgRetryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryThreeIntervalMin}")
            String orgRetryThreeIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryOneIntervalMin}")
            String userRetryOneIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryTwoIntervalMin}")
            String userRetryTwoIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefresh.retryThreeIntervalMin}")
            String userRetryThreeIntervalMin,
            @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
            String activeUserRefreshDays,
            @Value("${professional.refdata.pageSize}")
            String pageSize,
            @Value("${groupAccess.lastRunTimeTolerance}")
            String tolerance,
            @Value("${professional.role.mapping.refreshApi.enabled}")
            boolean refreshApiEnabled) {
        this.orgRetryOneIntervalMin = orgRetryOneIntervalMin;
        this.orgRetryTwoIntervalMin = orgRetryTwoIntervalMin;
        this.orgRetryThreeIntervalMin = orgRetryThreeIntervalMin;
        this.userRetryOneIntervalMin = userRetryOneIntervalMin;
        this.userRetryTwoIntervalMin = userRetryTwoIntervalMin;
        this.userRetryThreeIntervalMin = userRetryThreeIntervalMin;
        this.activeUserRefreshDays = activeUserRefreshDays;
        this.pageSize = pageSize;
        this.tolerance = tolerance;
        this.refreshApiEnabled = refreshApiEnabled;
    }
}
