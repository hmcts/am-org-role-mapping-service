package uk.gov.hmcts.reform.orgrolemapping.config;

import org.springframework.beans.factory.annotation.Value;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class ProfessionalUserServiceConfig {

    @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryOneIntervalMin}")
    public String retryOneIntervalMin;
    
    @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryTwoIntervalMin}")
    public String retryTwoIntervalMin;
    
    @Value("${professional.role.mapping.scheduling.findUsersWithStaleOrganisations.retryThreeIntervalMin}")
    public String retryThreeIntervalMin;

    @Value("${professional.role.mapping.scheduling.userRefresh.retryOneIntervalMin}")
    public String userRetryOneIntervalMin;

    @Value("${professional.role.mapping.scheduling.userRefresh.retryTwoIntervalMin}")
    public String userRetryTwoIntervalMin;

    @Value("${professional.role.mapping.scheduling.userRefresh.retryThreeIntervalMin}")
    public String userRetryThreeIntervalMin;

    @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
    public String activeUserRefreshDays;

    @Value("${professional.refdata.pageSize}")
    public String pageSize;

    @Value("${groupAccess.lastRunTimeTolerance}")
    public String tolerance;
}
