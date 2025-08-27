package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RestructuredAccessTypes;
import uk.gov.hmcts.reform.orgrolemapping.helper.JsonHelper;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.oidc.IdamRepository;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.scheduler.BaseSchedulerTestIntegration.TEST_ENVIRONMENT;
import static uk.gov.hmcts.reform.orgrolemapping.scheduler.BaseSchedulerTestIntegration.TEST_PAGE_SIZE;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.writeValueAsPrettyJson;

@Slf4j
@TestPropertySource(properties = {
    // turn off service bus
    "amqp.crd.enabled=false",
    "amqp.jrd.enabled=false",
    // set environment for pagesize
    "professional.refdata.pageSize=" + TEST_PAGE_SIZE,
    // set environment ready for flag checks
    "orm.environment=" + TEST_ENVIRONMENT,
    "testing.support.enabled=true"
})
public class BaseSchedulerTestIntegration extends BaseTestIntegration {

    static final String TEST_ENVIRONMENT = "local";
    static final String TEST_PAGE_SIZE = "3";

    static final String DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN";
    static final String DUMMY_S2S_TOKEN = "DUMMY_S2S_TOKEN";

    public static final UUID STUB_ID_RAS_CREATE_ROLEASSIGNMENTS
            = UUID.fromString("0bfabe25-fd57-4f8a-9882-911b53857258");
    public static final UUID STUB_ID_PRD_REFRESH_USER
            = UUID.fromString("491482e1-a8ec-4170-b986-177259e152cd");

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

    @SneakyThrows
    protected RestructuredAccessTypes extractAccessTypes(AccessTypesEntity accessTypesEntity) {
        return MAPPER.readValue(accessTypesEntity.getAccessTypes(), new TypeReference<>() {});
    }

    protected void logObject(String message, Object object) {
        log.info("{}: {}", message, writeValueAsPrettyJson(object));
    }

    protected void logOrganisationProfiles(AccessTypesEntity accessTypesEntity) {
        RestructuredAccessTypes accessTypes = extractAccessTypes(accessTypesEntity);

        if (CollectionUtils.isEmpty(accessTypes.getOrganisationProfiles())) {
            log.info("No Organisation Profiles found");
        } else {
            for (OrganisationProfile orgProfile: accessTypes.getOrganisationProfiles()) {
                log.info("-----------------------------------------------------");
                log.info("OrganisationProfileId = {}: {}",
                        orgProfile.getOrganisationProfileId(),
                        writeValueAsPrettyJson(orgProfile)
                );
                log.info("-----------------------------------------------------");
            }
        }

    }

    protected List<ServeEvent> logWiremockPostCalls(UUID stubId) {
        var allServeEvents = WIRE_MOCK_SERVER.getServeEvents(ServeEventQuery.forStubMapping(stubId)).getServeEvents();
        log.info("#####################################################");

        int counter = 0;
        for (ServeEvent event : allServeEvents) {
            log.info("{}: request number {}:", event.getStubMapping().getName(), counter++);

            logLoggedRequest(event.getRequest());
            logLoggedResponse(event.getResponse());
        }

        log.info("#####################################################");

        return allServeEvents;
    }

    private void logLoggedRequest(LoggedRequest loggedRequest) {

        log.info("-----------------------------------------------------");
        log.info("Request: {}: '{}'", loggedRequest.getMethod().getName(), loggedRequest.getUrl());

        log.info("   Headers: ");
        List<String> headersToLog = List.of(
                AUTHORIZATION,
                SERVICE_AUTHORIZATION,
                "Content-Type",
                "Accept"
        );
        for (String header : headersToLog) {
            log.info("      {}: {}", header, loggedRequest.getHeaders().getHeader(header));
        }

        log.info("   QueryParameters: {}", loggedRequest.getQueryParams());
        log.info("   Body: {}", loggedRequest.getBodyAsString());
        log.info("-----------------------------------------------------");
    }

    private void logLoggedResponse(LoggedResponse loggedResponse) {

        log.info("-----------------------------------------------------");
        log.info("Response: ");

        log.info("   Status: {}", loggedResponse.getStatus());
        log.info("   Body: {}", loggedResponse.getBodyAsString());
    }

    protected void stubPrdRefreshUser(List<String> fileNames, String userId,
                                        String moreAvailable, String lastRecordInPage) {
        stubPrdRefreshUser(
                "{ \"users\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                        + ", \"moreAvailable\": " + moreAvailable
                        + ", \"lastRecordInPage\": " + lastRecordInPage
                        + " }", userId
        );
    }

    protected void stubPrdRefreshUser(String body, String userId) {
        HttpHeaders headers = new HttpHeaders()
                .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
                .plus(new HttpHeader("userId", userId));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(
                "/refdata/internal/v1/organisations/users"))
                .withId(STUB_ID_PRD_REFRESH_USER)
                .withQueryParam("userId", equalTo(userId))
                .withQueryParam("since", absent())
                .withQueryParam("pageSize", absent())
                .withQueryParam("searchAfter", absent())
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeaders(headers)
                        .withBody(body)));
    }


    protected void stubRasCreateRoleAssignment(List<String> fileNames,
                                               EndStatus endStatus) {
        stubRasCreateRoleAssignment(
                fileNames.size() == 0 ? "{}" :
                        """
                        {
                            "links": [],
                            "roleAssignmentResponse": {
                                "roleRequest": {
                                    "id": "2fe5b5fb-fb01-4398-85ce-bbe34b7f374c",
                                    "authenticatedUserId": "5ff9f67c-8605-428d-96b8-9ea7ac8e99b9",
                                    "correlationId": "01f6e7e2-c66c-44a0-a7e4-73c1507c92b7",
                                    "assignerId": "5ff9f67c-8605-428d-96b8-9ea7ac8e99b9",
                                    "requestType": "CREATE",
                                    "process": "businessProcess1",
                                    "reference": "50b143cb-5644-4103-b37f-ee7005ca24d6",
                                    "replaceExisting": true,
                                    "status": "APPROVED",
                                    "created": "2020-11-19T11:42:13.454994",
                                    "log": "Request has been Approved"
                                },
                                "requestedRoles": """ + jsonHelper.readJsonArrayFromFiles(fileNames) + """
                }
            }""",
                endStatus
        );
    }

    protected void stubRasCreateRoleAssignment(String body,
                                               EndStatus endStatus) {
        HttpHeaders headers = new HttpHeaders()
                .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

        int httpStatus = EndStatus.FAILED.equals(endStatus)
                ? HttpStatus.UNAUTHORIZED.value() : HttpStatus.OK.value();

        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
                "/am/role-assignments"))
                .withId(STUB_ID_RAS_CREATE_ROLEASSIGNMENTS)
                .willReturn(aResponse()
                        .withStatus(httpStatus)
                        .withHeaders(headers)
                        .withBody(body)));
    }

}