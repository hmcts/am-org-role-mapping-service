package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.helper.JsonHelper;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.reform.orgrolemapping.scheduler.BaseSchedulerTestIntegration.TEST_ENVIRONMENT;
import static uk.gov.hmcts.reform.orgrolemapping.scheduler.BaseSchedulerTestIntegration.TEST_PAGE_SIZE;

@Slf4j
@TestPropertySource(properties = {
        // turn off service bus
        "amqp.crd.enabled=false",
        "amqp.jrd.enabled=false",
        // set environment for pagesize
        "professional.refdata.pageSize=" + TEST_PAGE_SIZE,
        // set environment ready for flag checks
        "orm.environment=" + TEST_ENVIRONMENT,
        "professional.role.mapping.refreshApi.enabled=true",
        "testing.support.enabled=true"
})
abstract class BaseSchedulerTestIntegration extends BaseTestIntegration {

    static final String TEST_ENVIRONMENT = "local";
    static final String TEST_PAGE_SIZE = "3";

    static final String DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN";
    static final String DUMMY_S2S_TOKEN = "DUMMY_S2S_TOKEN";

    protected final JsonHelper jsonHelper = new JsonHelper();
    protected final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    @InjectMocks
    private SecurityUtils securityUtils;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    private IdamRepository idamRepository;

    @BeforeEach
    public void setUp() throws Exception {

        // NB: THis is a test for a scheduled job so there will be no SecurityContext loaded from a request
        SecurityContextHolder.clearContext();

        doReturn(DUMMY_AUTH_TOKEN).when(idamRepository).getUserToken();
        doReturn(DUMMY_S2S_TOKEN).when(authTokenGenerator).generate();

        wiremockFixtures.resetRequests();
    }
}
