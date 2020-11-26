package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.CRDFeignClientFallback;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    private CRDFeignClientFallback crdFeignClientFallback;

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
    private WelcomeController welcomeController;

    private static final String RAS_ONE_USER_ONE_ROLE = "RASOneUserOneRole";
    private static final String RAS_ONE_USER_MULTI_ROLE = "RASOneUserMultiRole";
    private static final String RAS_MULTI_USER_ONE_ROLE = "RASMultiUserOneRole";
    private static final String RAS_DELETE_FLAG_TRUE = "RASDeleteFlagTrue";
    private static final String RAS_DROOL_RULE_FAIL = "RASDroolRuleFail";
    private static final String RAS_UPDATE_ROLE_TCW_STCW = "RASUpdateRoleTcwStcw";

    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";

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
    @DisplayName("S1: must successfully create org role mapping for single user with one role assignment")
    public void createOrgRoleMappingForSingleUserWithOneRoleAssignment() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2",
                                ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",
                                false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S2: must successfully create org role mapping for single user with multiple role assignments")
    public void createOrgRoleMappingForSingleUserWithMultipleRoleAssignment() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, true,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",
                                false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445676"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_MULTI_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S3: must successfully create org role mapping for multiple users each has single role assignment")
    public void createOrgRoleMappingForMultipleUsersWithOneRoleAssignment() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(true, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",
                                false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_MULTI_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S6: must successfully delete org role mapping when delete flag is true")
    public void createOrgRoleMappingDeleteOrgRoleMappingTrue() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",true), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9v"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DELETE_FLAG_TRUE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 0, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S8: must receive a rejected response when drool rules fail in RAS")
    public void createOrgRoleMappingErrorWhenDroolsFail() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DROOL_RULE_FAIL);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.REJECTED, 1, Status.CREATE_APPROVED, request.getUsers());
    }

    @Test
    @DisplayName("S9: must successfully create org role mapping for an update of role TCW to STCW")
    public void createOrgRoleMappingUpdateRole() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445000"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_UPDATE_ROLE_TCW_STCW);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ROLE_NAME_STCW));

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S11: must receive an error message when there is no primary location")
    public void createOrgRoleMappingErrorWhenNoPrimaryLocation() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, false, false,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(400))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("The user has 0 primary location(s), only 1 is allowed"));
    }

    @Test
    @DisplayName("S12: must receive an error message when no base location list is provided")
    public void createOrgRoleMappingErrorWhenNoLocationList() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                false, true, true,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(400))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("The base location is not available"));
    }

    @Test
    @DisplayName("S13: must receive an error message when base location has more than one primary")
    public void createOrgRoleMappingErrorWhenMultiPrimaryLocation() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, true,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(400))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("The user has 2 primary location(s), only 1 is allowed"));
    }

    @Test
    @DisplayName("S16: must receive an error message when no work area list is provided")
    public void createOrgRoleMappingErrorWhenNoWorkArea() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,"1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, true,
                                false,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(400))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("The work area is not available"));
    }

    @Test
    @DisplayName("S17: must receive an error message when no users provided")
    public void createOrgRoleMappingErrorWhenNoUsers() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,
                                "1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                                true, true, false,
                                true,"1", "2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(new ArrayList<>())
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(404))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("Resource not found Some of the user profiles couldn't be found"));
    }

    @Test
    @DisplayName("S18: must return empty list of requestedRoles when invalid roleId provided")
    public void createOrgRoleMappingErrorWhenInvalidRole() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,
                                "3", "2", "Invalid Role Name", ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DELETE_FLAG_TRUE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 0, Status.LIVE, request.getUsers());
    }

    @Test
    @DisplayName("S19: drools must map correct role name based on roleId")
    public void createOrgRoleMappingDroolsMustMapCorrectRoleName() throws Exception {

        Mockito.when(crdFeignClientFallback.createRoleAssignment(any()))
                .thenReturn(new ResponseEntity<>(IntTestDataBuilder
                        .buildListOfUserProfiles(false, false,
                                "1", "2","ROLE_NAME_TCW", ROLE_NAME_TCW,
                                true, true, false,
                                true,"BFA1", "BFA2",false), HttpStatus.OK));

        UserRequest request = UserRequest.builder()
                .users(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("senior-tribunal-caseworker"));

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUsers());
    }

    public void setRoleAssignmentWireMock(HttpStatus status, String fileName) throws IOException {
        String body = null;
        int returnHttpStatus = status.value();
        if (status.is2xxSuccessful()) {
            body = readJsonFromFile(fileName);
            returnHttpStatus = 201;
        }

        roleAssignmentService.stubFor(WireMock.post(urlEqualTo("/am/role-assignments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)
                        .withStatus(returnHttpStatus)
                ));

        List<String> userRequestList = Arrays.asList(
                UUID.randomUUID().toString(), UUID.randomUUID().toString()
        );
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        //this stub is overruled by the mocking of its bean at the top of this test class
        crdClient.stubFor(WireMock.post(urlEqualTo("/refdata/case-worker/users/fetchUsersById"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(new CRDFeignClientFallback()
                                .createRoleAssignment(new UserRequest(userRequestList)).getBody()))
                        .withStatus(returnHttpStatus)
                ));
    }

    private String readJsonFromFile(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = WelcomeControllerIntegrationTest.class
                .getResourceAsStream(String.format("/%s.json", fileName));
        Object json = mapper.readValue(is, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    private void assertResponse(MvcResult result, Status requestStatus, int roleAssignmentCount,
                                Status roleAssingmentStatus, List<String> userIds)
            throws UnsupportedEncodingException, JsonProcessingException {

        List<String> actorIds = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        String contentAsString = result.getResponse().getContentAsString();

        JsonNode responseJsonNode = objectMapper.readValue(contentAsString,
                JsonNode.class);
        JsonNode responseNode = responseJsonNode.get(0).get("roleAssignmentResponse");

        assertEquals(requestStatus.toString(), responseNode.get("roleRequest").get("status").asText());
        assertEquals(roleAssignmentCount, responseNode.get("requestedRoles").size());
        if(roleAssignmentCount > 0) {
            responseNode.get("requestedRoles").forEach(requestedRole -> {
                assertEquals(roleAssingmentStatus.toString(), requestedRole.get("status").asText());
                if (!actorIds.contains(requestedRole.get("actorId").asText())) {
                    actorIds.add(requestedRole.get("actorId").asText());
                }
            });
            assertEquals(userIds, actorIds);
        }
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

