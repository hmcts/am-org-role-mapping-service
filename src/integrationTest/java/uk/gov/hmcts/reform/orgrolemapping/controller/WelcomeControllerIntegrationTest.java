package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WelcomeControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeControllerIntegrationTest.class);

    private transient MockMvc mockMvc;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private SecurityUtils securityUtils;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluator;

    @MockBean
    private FeignClientInterceptor feignClientInterceptor;

    @Inject
    private JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Inject
    private WebApplicationContext wac;

    @ClassRule
    public static WireMockRule roleAssignmentService = new WireMockRule(wireMockConfig().port(4096));

    @ClassRule
    public static final WireMockRule crdClient = new WireMockRule(wireMockConfig().port(4099));

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @Autowired
    private transient WelcomeController welcomeController;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        //this.mockMvc = standaloneSetup(this.welcomeController).build()

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        UserInfo userInfo = UserInfo.builder()
                .uid("6b36bfc6-bb21-11ea-b3de-0242ac130006")
                .sub("emailId@a.com")
                .build();
        ReflectionTestUtils.setField(
                jwtGrantedAuthoritiesConverter,
                "userInfo", userInfo

        );
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        doReturn(true).when(featureConditionEvaluator).preHandle(any(), any(), any());
    }

    @Test
    public void welcomeApiTest() throws Exception {
        final String url = "/welcome";
        logger.info(" WelcomeControllerIntegrationTest : Inside  Welcome API Test method...{}", url);
        final MvcResult result = mockMvc.perform(get(url).contentType(JSON_CONTENT_TYPE))
                .andExpect(status().is(200))
                .andReturn();
        assertEquals(
                "Welcome service message", "Welcome to Organisation Role Mapping Service",
                result.getResponse().getContentAsString());
    }


    @Test
     public void createOrgRoleMappingTest() throws Exception {
        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c", "21334a2b-79ce-44eb-9168-2d49a744be9d"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED);

        mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request))
        ).andExpect(status().is(200)).andReturn();
    }


    public void setRoleAssignmentWireMock(HttpStatus status) throws JsonProcessingException {
        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful()) {
            body = "{\n"
                    + "    \"links\": [],\n"
                    + "    \"roleAssignmentResponse\": {\n"
                    + "    \"roleRequest\": {\n"
                    + "    \"id\": \"9bba5a4b-dbbc-4a47-bc8e-3eb75aa195b6\",\n"
                    + "    \"authenticatedUserId\": \"6eb64a6f-8273-4cdf-9b72-0a0ae4f9444f\",\n"
                    + "    \"correlationId\": \"4abf3f30-b033-47f8-9af3-ccaa61e77c72\",\n"
                    + "    \"assignerId\": \"123e4567-e89b-42d3-a456-556642445678\",\n"
                    + "    \"requestType\": \"CREATE\",\n"
                    + "    \"process\": \"p2\",\n"
                    + "    \"reference\": \"p2\",\n"
                    + "          \"replaceExisting\": false,\n"
                    + "            \"status\": \"APPROVED\",\n"
                    + "            \"created\": \"2020-10-13T15:18:45.263721\",\n"
                    + "            \"log\": \"Request has been approved by rule : R12_role_validation\"\n"
                    + "        },\n"
                    + "        \"requestedRoles\": [\n"
                    + "            {\n"
                    + "                \"id\": \"ab26d584-e37b-4db9-98fa-ce5e2e7a7653\",\n"
                    + "                \"actorIdType\": \"IDAM\",\n"
                    + "                \"actorId\": \"123e4567-e89b-42d3-a456-556642445612\",\n"
                    + "                \"roleType\": \"CASE\",\n"
                    + "                \"roleName\": \"judge\",\n"
                    + "                \"classification\": \"PUBLIC\",\n"
                    + "                \"grantType\": \"SPECIFIC\",\n"
                    + "                \"roleCategory\": \"JUDICIAL\",\n"
                    + "                \"readOnly\": false,\n"
                    + "                \"process\": \"p2\",\n"
                    + "                \"reference\": \"p2\",\n"
                    + "                \"statusSequence\": 10,\n"
                    + "                \"status\": \"LIVE\",\n"
                    + "                \"created\": \"2020-10-13T15:18:45.263789\",\n"
                    + "                \"log\": \"Requested Role has been approved by rule : R12_role_validation \",\n"
                    + "                \"attributes\": {\n"
                    + "                    \"contractType\": \"SALARIED\",\n"
                    + "                    \"jurisdiction\": \"divorce\",\n"
                    + "                    \"caseId\": \"1234567890123456\",\n"
                    + "                    \"region\": \"north-east\"\n"
                    + "                }\n"
                    + "            }\n"
                    + "        ]\n"
                    + "    }\n"
                    + "}";
            returnHttpStaus = 201;
        }

        roleAssignmentService.stubFor(WireMock.post(urlEqualTo("/am/role-assignments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStaus)
                ));

        List<String> userRequestList = Arrays.asList(
                UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        crdClient.stubFor(WireMock.post(urlEqualTo("/refdata/case-worker/users/fetchUsersById"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(new CRDFeignClientFallback()
                                .createRoleAssignment(new UserRequest(userRequestList)).getBody()))
                        .withStatus(returnHttpStaus)
                ));
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String authorisation = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIs";
        headers.set("Authorization", "Bearer " + authorisation);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String s2SToken = MockUtils.generateDummyS2SToken("am_org_role_mapping_service");
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        return headers;
    }
}

