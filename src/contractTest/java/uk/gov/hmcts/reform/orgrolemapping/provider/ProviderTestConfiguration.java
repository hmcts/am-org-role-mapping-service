package uk.gov.hmcts.reform.orgrolemapping.provider;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RetrieveDataService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ParseRequestService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialBookingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;

import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@TestConfiguration
public class ProviderTestConfiguration {

    @MockBean
    CRDService crdService;

    @MockBean
    JRDService jrdService;

    @MockBean
    PRDService prdService;

    @MockBean
    AccessTypesRepository accessTypesRepository;

    @MockBean
    UserRefreshQueueRepository userRefreshQueueRepository;

    @Bean
    @Primary
    public RetrieveDataService getRetrieveDataService() {
        return new RetrieveDataService(getParseRequestService(), crdService, jrdService, false, false);
    }

    @Bean
    @Primary
    public RequestMappingService<UserAccessProfile> getRequestMappingService() {
        return new RequestMappingService<>(
                "pr", persistenceService, roleAssignmentService, getStatelessKieSession(), securityUtils);
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

    @MockBean
    SecurityUtils securityUtils;

    @MockBean
    private CacheManager cacheManager;

    @MockBean
    JudicialBookingService judicialBookingService;

    @MockBean
    ObjectMapper objectMapper;

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
                "1",
                "descending",
                "1",
                List.of("am_org_role_mapping_service", "am_role_assignment_refresh_batch")
        );
    }

    @Bean
    @Primary
    public JudicialRefreshOrchestrator judicialRefreshOrchestrator() {
        return new JudicialRefreshOrchestrator(getRetrieveDataService(), getParseRequestService(),
                judicialBookingService, getRequestMappingService());
    }

    @Bean
    @Primary
    public ProfessionalRefreshOrchestrator professionalRefreshOrchestrator() {
        return new ProfessionalRefreshOrchestrator(accessTypesRepository, userRefreshQueueRepository, prdService,
                objectMapper,
                roleAssignmentService,
                securityUtils
                );
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
