package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Status;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration.FeignClientInterceptor;
import uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.helper.UserAccessProfileBuilder.buildJudicialProfile;


@TestPropertySource(properties = {"dbFeature.flags.enable=iac_jrd_1_0"})
public class WelcomeControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeControllerIntegrationTest.class);

    private transient MockMvc mockMvc;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private CRDFeignClient crdFeignClient;

    @MockBean
    private JRDFeignClient jrdFeignClient;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluator;

    @MockBean
    private FeignClientInterceptor feignClientInterceptor;

    @MockBean
    private CRDTopicConsumer crdTopicConsumer;

    @MockBean
    private JRDTopicConsumer jrdTopicConsumer;


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
    private static final String RAS_RESPONSE_TRIBUNAL_JUDGE_FEE_PAID = "RASTribunalJudgeFeePaid";
    private static final String RAS_RESPONSE_TRIBUNAL_JUDGE_SALARIED_PAID = "RASTribunalJudgeSalaried";
    private static final String RAS_RESPONSE_ASSISTANT_RESIDENT_JUDGE_ROLES = "RASAssistantResidentJudgeRoles";
    private static final String RAS_ONE_USER_MULTI_ROLE = "RASOneUserMultiRole";
    private static final String RAS_MULTI_USER_ONE_ROLE = "RASMultiUserOneRole";
    private static final String RAS_DELETE_FLAG_TRUE = "RASDeleteFlagTrue";
    private static final String RAS_DROOL_RULE_FAIL = "RASDroolRuleFail";
    private static final String RAS_UPDATE_ROLE_TCW_STCW = "RASUpdateRoleTcwStcw";

    private static final String ROLE_NAME_STCW = "senior-tribunal-caseworker";
    private static final String ROLE_NAME_TCW = "tribunal-caseworker";
    UserRequest userRequest;
    List<JudicialProfile> judicialProfiles;

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
        userRequest = UserRequest.builder().userIds(Arrays.asList("4dc7dd3c-3fb5-4611-bbde-5101a97681e2"))
                .build();

        judicialProfiles = new ArrayList<>(buildJudicialProfile(userRequest,
                "judicialProfileSample.json"));
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


    @DisplayName("S1: must successfully create org role mapping for single user with one role assignment")
    public void createOrgRoleMappingForSingleUserWithOneRoleAssignment() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2",
                        ROLE_NAME_STCW, ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S2: must successfully create org role mapping for single user with multiple role assignments")
    public void createOrgRoleMappingForSingleUserWithMultipleRoleAssignment() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, true, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445676"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_MULTI_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S3: must successfully create org role mapping for multiple users each has single role assignment")
    public void createOrgRoleMappingForMultipleUsersWithOneRoleAssignment() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(true, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2",
                        false), HttpStatus.OK)).when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000", "123e4567-e89b-42d3-a456-556642445111"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_MULTI_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S6: must successfully delete org role mapping when delete flag is true")
    public void createOrgRoleMappingDeleteOrgRoleMappingTrue() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2", true), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());


        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9v"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DELETE_FLAG_TRUE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 0, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S8: must receive a rejected response when drool rules fail in RAS")
    public void createOrgRoleMappingErrorWhenDroolsFail() throws Exception {

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DROOL_RULE_FAIL);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.REJECTED, 1, Status.CREATE_APPROVED, request.getUserIds());
    }

    @Test
    @DisplayName("S9: must successfully create org role mapping for an update of role TCW to STCW")
    public void createOrgRoleMappingUpdateRole() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445000"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_UPDATE_ROLE_TCW_STCW);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains(ROLE_NAME_STCW));

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S11: must receive an error message when there is no primary location")
    public void createOrgRoleMappingErrorWhenNoPrimaryLocation() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, false, false,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertNotNull(contentAsString);
    }

    @Test
    @DisplayName("S12: must receive an error message when no base location list is provided")
    public void createOrgRoleMappingErrorWhenNoLocationList() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        false, true, true,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertNotNull(contentAsString);
    }

    @Test
    @DisplayName("S13: must receive an error message when base location has more than one primary")
    public void createOrgRoleMappingErrorWhenMultiPrimaryLocation() throws Exception {

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, true,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertNotNull(contentAsString);
    }

    @Test
    @DisplayName("S16: must receive an error message when no work area list is provided")
    public void createOrgRoleMappingErrorWhenNoWorkArea() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false, "1", "2", ROLE_NAME_STCW,
                        ROLE_NAME_TCW,
                        true, true, true,
                        false, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertNotNull(contentAsString);
    }

    @Test
    @DisplayName("S17: must receive an error message when no users provided")
    public void createOrgRoleMappingErrorWhenNoUsers() throws Exception {

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false,
                        "1", "2", ROLE_NAME_STCW, ROLE_NAME_TCW,
                        true, true, false,
                        true, "1", "2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(new ArrayList<>())
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(400))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("Empty user request"));
    }

    @Test
    @DisplayName("S18: must return empty list of requestedRoles when invalid roleId provided")
    public void createOrgRoleMappingErrorWhenInvalidRole() throws Exception {

        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false,
                        "3", "2", "Invalid Role Name", ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_DELETE_FLAG_TRUE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        assertResponse(result, Status.APPROVED, 0, Status.LIVE, request.getUserIds());
    }

    @Test
    @DisplayName("S19: drools must map correct role name based on roleId")
    public void createOrgRoleMappingDroolsMustMapCorrectRoleName() throws Exception {


        doReturn(new ResponseEntity<>(IntTestDataBuilder
                .buildListOfUserProfiles(false, false,
                        "1", "2", "ROLE_NAME_TCW", ROLE_NAME_TCW,
                        true, true, false,
                        true, "BFA1", "BFA2", false), HttpStatus.OK))
                .when(crdFeignClient).getCaseworkerDetailsById(any());

        UserRequest request = UserRequest.builder()
                .userIds(Arrays.asList("123e4567-e89b-42d3-a456-556642445674"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_ONE_USER_ONE_ROLE);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("CASEWORKER"))
                .content(mapper.writeValueAsBytes(request)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("senior-tribunal-caseworker"));

        assertResponse(result, Status.APPROVED, 1, Status.LIVE, request.getUserIds());
    }

    /*
        IT FOR JRD Scenarios Start from here
     */

    @Test
    @DisplayName("S20: drools must map correct roles name based on appointments & appointment type")
    public void createOrgRolesForIACTribunalJudge_FeePaidThroughMapping() throws Exception {


        judicialProfiles.get(0).getAppointments().remove(0);

        doReturn(ResponseEntity.ok(judicialProfiles))
                .when(jrdFeignClient).getJudicialDetailsById(any());


        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_RESPONSE_TRIBUNAL_JUDGE_FEE_PAID);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("JUDICIAL"))
                .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("hmcts-judiciary"));
        assertTrue(contentAsString.contains("fee-paid-judge"));

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, userRequest.getUserIds());
    }

    @Test
    @DisplayName("S21: drools must map the correct roles based on default lower level for tribunal judge salaried")
    public void createOrgRolesForIACTribunalJudge_SalariedThroughMapping() throws Exception {


        judicialProfiles.get(0).getAppointments().remove(1);

        doReturn(ResponseEntity.ok(judicialProfiles))
                .when(jrdFeignClient).getJudicialDetailsById(any());


        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_RESPONSE_TRIBUNAL_JUDGE_SALARIED_PAID);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("JUDICIAL"))
                .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("hmcts-judiciary"));
        assertTrue(contentAsString.contains("case-allocator"));
        assertTrue(contentAsString.contains("judge"));

        assertResponse(result, Status.APPROVED, 3, Status.LIVE, userRequest.getUserIds());
    }

    @Test
    @DisplayName("S22: drool must map the correct roles based on IACAssistantResidentJudge Role ")
    public void createOrgRolesForIACAssistantResidentJudgeRolesThroughMapping() throws Exception {


        judicialProfiles.get(0).getAppointments().remove(1);
        judicialProfiles.get(0).getAppointments().forEach(appointment ->
                appointment.setRoles(Arrays.asList("Assistant Resident Judge"))
        );

        doReturn(ResponseEntity.ok(judicialProfiles))
                .when(jrdFeignClient).getJudicialDetailsById(any());


        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_RESPONSE_ASSISTANT_RESIDENT_JUDGE_ROLES);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("JUDICIAL"))
                .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("hmcts-judiciary"));
        assertTrue(contentAsString.contains("case-allocator"));
        assertTrue(contentAsString.contains("judge"));
        assertTrue(contentAsString.contains("leadership-judge"));
        assertTrue(contentAsString.contains("task-supervisor"));

        assertResponse(result, Status.APPROVED, 5, Status.LIVE, userRequest.getUserIds());
    }


    @Test
    @DisplayName("S23: drools must map the correct roles  based on iac authorisations for Tribunal Judge fee-paid")
    public void createOrgRolesForTribunalJudge_FeePaidByIacAuthorisation() throws Exception {


        judicialProfiles.get(0).getAppointments().remove(0);
        judicialProfiles.get(0).getAppointments().forEach(appointment ->
                appointment.setServiceCode(null)
        );

        doReturn(ResponseEntity.ok(judicialProfiles))
                .when(jrdFeignClient).getJudicialDetailsById(any());


        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED, RAS_RESPONSE_TRIBUNAL_JUDGE_FEE_PAID);

        MvcResult result = mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders("JUDICIAL"))
                .content(mapper.writeValueAsBytes(userRequest)))
                .andExpect(status().is(200))
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("hmcts-judiciary"));
        assertTrue(contentAsString.contains("fee-paid-judge"));

        assertResponse(result, Status.APPROVED, 2, Status.LIVE, userRequest.getUserIds());
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
        JsonNode responseNode = null;

        if (StringUtils.isNotEmpty(contentAsString) && !contentAsString.equals("[]")) {
            JsonNode responseJsonNode = objectMapper.readValue(contentAsString,
                    JsonNode.class);

            responseNode = responseJsonNode.get(0).get("roleAssignmentResponse");

            assertEquals(requestStatus.toString(), responseNode.get("roleRequest").get("status").asText());
            assertEquals(roleAssignmentCount, responseNode.get("requestedRoles").size());
        }
        if (roleAssignmentCount > 0) {
            responseNode.get("requestedRoles").forEach(requestedRole -> {
                assertEquals(roleAssingmentStatus.toString(), requestedRole.get("status").asText());
                if (!actorIds.contains(requestedRole.get("actorId").asText())) {
                    actorIds.add(requestedRole.get("actorId").asText());
                }
            });
            assertEquals(userIds, actorIds);
        }
    }

    private HttpHeaders getHttpHeaders(String userType) {
        HttpHeaders headers = new HttpHeaders();
        String authorisation = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIs";
        headers.set("Authorization", "Bearer " + authorisation);
        headers.set("userType", userType);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String s2SToken = MockUtils.generateDummyS2SToken("am_org_role_mapping_service");
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        return headers;
    }
}

