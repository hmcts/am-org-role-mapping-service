package uk.gov.hmcts.reform.orgrolemapping.provider;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.orgrolemapping.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialBookingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ParseRequestService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RetrieveDataService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.List;

import static org.mockito.Mockito.when;

@TestConfiguration
public class ProviderTestConfiguration {

    @MockBean
    CRDService crdService;

    @MockBean
    JRDService jrdService;

    @Bean
    @Primary
    public RetrieveDataService getRetrieveDataService() {
        return new RetrieveDataService(getParseRequestService(), crdService, jrdService, true);
    }

    @Bean
    @Primary
    public RequestMappingService<UserAccessProfile> getRequestMappingService() {
        return new RequestMappingService<>(persistenceService, getEnvironmentConfiguration(), roleAssignmentService,
                getStatelessKieSession(), securityUtils);
    }

    @MockBean
    RoleAssignmentService roleAssignmentService;

    @MockBean
    PersistenceService persistenceService;

    @Bean
    @Primary
    public ParseRequestService getParseRequestService() {
        return new ParseRequestService();
    }

    @Bean
    @Primary
    public EnvironmentConfiguration getEnvironmentConfiguration() {
        EnvironmentConfiguration environmentConfiguration = Mockito.mock(EnvironmentConfiguration.class);
        when(environmentConfiguration.getEnvironment()).thenReturn("pr");
        return environmentConfiguration;
    }

    @MockBean
    SecurityUtils securityUtils;

    @MockBean
    private CacheManager cacheManager;

    @MockBean
    JudicialBookingService judicialBookingService;

    @Bean
    @Primary
    public RefreshOrchestrator refreshOrchestrator() {
        return new RefreshOrchestrator(
                getRetrieveDataService(),
                getRequestMappingService(),
                getParseRequestService(),
                crdService,
                persistenceService,
                securityUtils,
                judicialBookingService,
                "1",
                "descending",
                "1",
                List.of("am_org_role_mapping_service", "am_role_assignment_refresh_batch"),
                true
        );
    }

    @Bean
    @Primary
    public JudicialRefreshOrchestrator judicialRefreshOrchestrator() {
        return new JudicialRefreshOrchestrator(getRetrieveDataService(), getParseRequestService(),
                judicialBookingService, getRequestMappingService());
    }

    private KieServices kieServices = KieServices.Factory.get();

    @Bean
    public KieContainer kieContainer() {
        return kieServices.getKieClasspathContainer();
    }


    @Bean
    public StatelessKieSession getStatelessKieSession() {
        return kieContainer().newStatelessKieSession("org-role-mapping-validation-session");
    }
}
