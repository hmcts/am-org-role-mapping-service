package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.admin.model.ServeEventQuery;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.HashMap;
import java.util.Map;
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
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
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
abstract class BaseSchedulerTestIntegration extends BaseTestIntegration {

    static final String TEST_ENVIRONMENT = "local";
    static final String TEST_PAGE_SIZE = "3";

    static final String DUMMY_AUTH_TOKEN = "DUMMY_AUTH_TOKEN";
    static final String DUMMY_S2S_TOKEN = "DUMMY_S2S_TOKEN";

    public static final UUID STUB_ID_RAS_CREATE_ROLEASSIGNMENTS
        = UUID.fromString("0bfabe25-fd57-4f8a-9882-911b53857258");
    public static final UUID STUB_ID_PRD_REFRESH_USER
            = UUID.fromString("491482e1-a8ec-4170-b986-177259e152cd");
    public static final UUID STUB_ID_PRD_RETRIEVE_USERS
        = UUID.fromString("47f05020-f89c-46ea-93f4-063f09ba96c0");

    protected static final String MORE_AVAILABLE = "moreAvailable";
    protected static final String LAST_RECORD_IN_PAGE = "lastRecordInPage";
    protected static final String SEARCH_AFTER = "searchAfter";

    public static final UUID STUB_ID_RAS_RETRIEVE_USERSBYORG
            = UUID.fromString("8468dbb3-14b9-4fd2-b9d8-0620a8fc1e94");

    public static final String JURISDICTION_ID_CIVIL = "CIVIL";
    public static final String JURISDICTION_ID_PUBLICLAW = "PUBLICLAW";

    public static final String SOLICITOR_PROFILE = "SOLICITOR_PROFILE";
    public static final String OGD_PROFILE = "OGD_PROFILE";

    public static final UUID STUB_ID_CCD_RETRIEVE_ACCESS_TYPES
        = UUID.fromString("72134798-eba8-4840-b769-6435bd2afb1c");

    public static final UUID STUB_ID_PRD_RETRIEVE_USERSBYORG
        = UUID.fromString("8468dbb3-14b9-4fd2-b9d8-0620a8fc1e94");

    public static final UUID STUB_ID_PRD_RETRIEVE_ORGANISATIONS
        = UUID.fromString("f4f89a01-39fb-48ca-9c2a-a49f749d07af");

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
        log.info("-----------------------------------------------------");
    }

    protected void stubRasCreateRoleAssignment(EndStatus endStatus) {
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
                        .withBody("{}")));
    }

    protected void stubPrdRetrieveUsers(List<String> fileNames,
        String moreAvailable, String lastRecordInPage, String pageSize, String searchAfter) {
        stubPrdRetrieveUsers(
            "{ \"users\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                + ", \"moreAvailable\": " + moreAvailable
                + ", \"lastRecordInPage\": " + lastRecordInPage
                + " }", moreAvailable, lastRecordInPage,
            pageSize, searchAfter
        );
    }

    protected void stubPrdRetrieveUsers(String body, String moreAvailable,
        String lastRecordInPage, String pageSize, String searchAfter) {
        HttpHeaders headers = new HttpHeaders()
            .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .plus(new HttpHeader(MORE_AVAILABLE, moreAvailable))
            .plus(new HttpHeader(SEARCH_AFTER, searchAfter != null ? searchAfter : ""))
            .plus(new HttpHeader(LAST_RECORD_IN_PAGE, lastRecordInPage != null ? lastRecordInPage : ""));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(
            "/refdata/internal/v1/organisations/users"))
            .withId(STUB_ID_PRD_RETRIEVE_USERS)
            .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
            .withQueryParam("searchAfter", searchAfter != null ? equalTo(searchAfter) : absent())
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeaders(headers)
                .withBody(body)));
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

    protected void stubPrdRetrieveUsersByOrg(List<String> fileNames,
        String moreAvailable, String lastRecordInPage, EndStatus endStatus) {
        stubPrdRetrieveUsersByOrg(
            "{ \"organisationInfo\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                + ", \"moreAvailable\": " + moreAvailable + " }", moreAvailable,
            endStatus
        );
    }

    protected void stubPrdRetrieveUsersByOrg(String body, String moreAvailable,
        EndStatus endStatus) {
        HttpHeaders headers = new HttpHeaders()
            .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .plus(new HttpHeader(MORE_AVAILABLE, moreAvailable));

        if (EndStatus.SUCCESS.equals(endStatus)) {
            WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
                    "/refdata/internal/v2/organisations/users"))
                    .withId(STUB_ID_PRD_RETRIEVE_USERSBYORG)
                    .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeaders(headers)
                            .withBody(body)));
        } else if (EndStatus.FAILED.equals(endStatus)) {
            WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
                    "/refdata/internal/v2/organisations/users"))
                    .withId(STUB_ID_PRD_RETRIEVE_USERSBYORG)
                    .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.UNAUTHORIZED.value())
                            .withHeaders(headers)
                            .withBody(body)));
        } else if (EndStatus.PARTIAL_SUCCESS.equals(endStatus)) {
            WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
                    "/refdata/internal/v2/organisations/users"))
                    .withId(STUB_ID_PRD_RETRIEVE_USERSBYORG)
                    .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
                    .withRequestBody(equalToJson("""
                            {"organisationIdentifiers": ["1"]}
                            """))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeaders(headers)
                            .withBody(body)));
            WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
                    "/refdata/internal/v2/organisations/users"))
                    .withId(STUB_ID_PRD_RETRIEVE_USERSBYORG)
                    .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
                    .withRequestBody(equalToJson("""
                            {"organisationIdentifiers": ["4"]}
                            """))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.UNAUTHORIZED.value())
                            .withHeaders(headers)
                            .withBody(body)));
        }
    }

    protected Map<String,String> getAccessTypesMap(String accessTypes) {
        Map<String,String> map = new HashMap<>();
        if (accessTypes != null && !accessTypes.isEmpty()) {
            String[] keyValuePairs = accessTypes
                .replace("{", "").replace("}", "")
                .replace("[", "").replace("]", "")
                .replace("\n","").replace("\r","")
                .split(",");
            if (keyValuePairs.length > 1) {
                for (String pair : keyValuePairs) {
                    String[] entry = pair.split(":");
                    map.put(entry[0].trim(), entry[1].trim());
                }
            }
        }
        return map;
    }

    protected void stubCcdRetrieveAccessTypes(List<String> jurisdictionFileNames) {
        stubCcdRetrieveAccessTypes(
            "{ \"jurisdictions\": " + jsonHelper.readJsonArrayFromFiles(jurisdictionFileNames) + " }"
        );
    }

    protected void stubCcdRetrieveAccessTypes(String body) {
        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching("/retrieve-access-types"))
            .withId(STUB_ID_CCD_RETRIEVE_ACCESS_TYPES)
            .withName("CCD Retrieve Access Types")
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    protected void stubPrdRetrieveOrganisations(List<String> fileNames,
        String moreAvailable, String lastRecordInPage) {
        stubPrdRetrieveOrganisations(
            "{ \"organisations\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                + ", \"moreAvailable\": " + moreAvailable + " }", moreAvailable
        );
    }

    protected void stubPrdRetrieveOrganisations(String body, String moreAvailable) {
        HttpHeaders headers = new HttpHeaders()
            .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .plus(new HttpHeader(MORE_AVAILABLE, moreAvailable));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching(
            "/refdata/internal/v1/organisations"))
            .withId(STUB_ID_PRD_RETRIEVE_ORGANISATIONS)
            .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeaders(headers)
                    .withBody(body)));
    }

    protected void stubPrdRetrieveOrganisationsByProfile(List<String> fileNames,
        String moreAvailable, String lastRecordInPage, String pageSize, String searchAfter) {
        stubPrdRetrieveOrganisationsByProfile(
            "{ \"organisationInfo\": " + jsonHelper.readJsonArrayFromFiles(fileNames)
                + ", \"moreAvailable\": " + moreAvailable
                + ", \"lastRecordInPage\": " + lastRecordInPage
                + " }", moreAvailable, lastRecordInPage,
            pageSize, searchAfter
        );
    }

    protected void stubPrdRetrieveOrganisationsByProfile(String body, String moreAvailable,
        String lastRecordInPage, String pageSize, String searchAfter) {
        HttpHeaders headers = new HttpHeaders()
            .plus(new HttpHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE))
            .plus(new HttpHeader(MORE_AVAILABLE, moreAvailable))
            .plus(new HttpHeader(SEARCH_AFTER, searchAfter != null ? searchAfter : ""))
            .plus(new HttpHeader(LAST_RECORD_IN_PAGE, lastRecordInPage != null ? lastRecordInPage : ""));

        WIRE_MOCK_SERVER.stubFor(post(urlPathMatching(
            "/refdata/internal/v1/organisations/getOrganisationsByProfile"))
            .withId(STUB_ID_PRD_RETRIEVE_ORGANISATIONS)
            .withQueryParam("pageSize", equalTo(TEST_PAGE_SIZE))
            .withQueryParam("searchAfter", searchAfter != null ? equalTo(searchAfter) : absent())
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeaders(headers)
                .withBody(body)));
    }

}
