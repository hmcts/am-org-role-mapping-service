package uk.gov.hmcts.reform.orgrolemapping.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.orgrolemapping.controller.RefreshController;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JRDService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialBookingService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.io.IOException;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Provider("am_orgRoleMapping_refresh")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}",
        consumerVersionSelectors = {@VersionSelector(tag = "master")})
@TestPropertySource(properties = {"spring.cache.type=none", "launchdarkly.sdk.environment=pr"})
@Import(ProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class OrgRoleMappingProviderTestForRefresh {

    @Autowired
    CRDService crdService;

    @Autowired
    JRDService jrdService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private RefreshOrchestrator refreshOrchestrator;

    @Autowired
    private JudicialRefreshOrchestrator judicialRefreshOrchestrator;

    @Autowired
    JudicialBookingService judicialBookingService;

    @Autowired
    RoleAssignmentService roleAssignmentService;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new RefreshController(
                refreshOrchestrator, judicialRefreshOrchestrator
        ));
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @State({"A refresh request is received with a valid userId passed"})
    public void refreshSingleValidUserId() throws IOException {
        initQueryMocks();
    }

    private void initQueryMocks() throws IOException {

        Mockito.when(securityUtils.getUserId()).thenReturn("5629957f-4dcd-40b8-a0b2-e64ff5898b28");

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        Mockito.when(jrdService.fetchJudicialProfiles(Mockito.any()))
                .thenReturn(new ResponseEntity<>(List.of(TestDataBuilder.buildJudicialProfile()), headers,
                        HttpStatus.OK));

        Mockito.when(judicialBookingService.fetchJudicialBookings(Mockito.any()))
                .thenReturn(List.of(TestDataBuilder.buildJudicialBooking()));

        ResponseEntity<Object> responseEntity =
                new ResponseEntity<>(TestDataBuilder.buildRoleAssignmentRequestResource(), HttpStatus.CREATED);

        Mockito.when(roleAssignmentService.createRoleAssignment(Mockito.any()))
                .thenReturn(responseEntity);

        Mockito.when(persistenceService.getStatusByParam(Mockito.any(), Mockito.any()))
                .thenReturn(true);

    }
}
