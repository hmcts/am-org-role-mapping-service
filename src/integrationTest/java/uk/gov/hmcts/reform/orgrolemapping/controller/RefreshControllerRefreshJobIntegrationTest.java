package uk.gov.hmcts.reform.orgrolemapping.controller;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;

import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;


import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.containsString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.PRD_USER_NOT_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.EXPECTED_SINGLE_PRD_USER;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=am_org_role_mapping_service,am_role_assignment_refresh_batch",
    "feign.client.config.jrdClient.v2Active=false"})
@Transactional
public class RefreshControllerRefreshJobIntegrationTest extends BaseTestIntegration {

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();
    private static final String AUTHORISED_SERVICE = "am_role_assignment_refresh_batch";
    private static final String PROFESSIONAL_REFRESH_URL = "/am/role-mapping/professional/refresh";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @Autowired
    private DataSource ds;

    @MockBean
    private RASFeignClient rasFeignClient;
    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private FeatureConditionEvaluator featureConditionEvaluation;

    @MockBean
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @BeforeEach
    public void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
    public void shouldProcessProfessionalRefreshRequest() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildRefreshUsersResponse("1234")))
            .when(prdFeignClient).getRefreshUsers(any());

        mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.Message").value(containsString(Constants.SUCCESS_ROLE_REFRESH)))
            .andReturn();
    }

    @Test
    public void shouldRejectProfessionalRefreshRequest_withoutUserId() throws Exception {
        mockMvc.perform(post(PROFESSIONAL_REFRESH_URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
            scripts = {"classpath:sql/delete_user_refresh_queue.sql"})
    public void shouldErrorProfessionalRefreshRequest_whenNoAccessTypesInDB() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildRefreshUsersResponse("1234")))
            .when(prdFeignClient).getRefreshUsers(any());

        MvcResult result = mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertTrue(result.getResolvedException() instanceof ServiceException);
        assertEquals(NO_ACCESS_TYPES_FOUND, result.getResolvedException().getMessage());
    }

    @Test
    public void shouldErrorProfessionalRefreshRequest_whenNoPRDUserFound() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.NotFound("Not Found", request, null, null))
            .when(prdFeignClient).getRefreshUsers(any());

        MvcResult result = mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
            .andExpect(status().isNotFound())
            .andReturn();

        assertTrue(result.getResolvedException() instanceof ResourceNotFoundException);
        assertEquals(String.format(Constants.RESOURCE_NOT_FOUND + " " + PRD_USER_NOT_FOUND, "1234"),
            result.getResolvedException().getMessage());
    }

    @Test
    public void shouldErrorProfessionalRefreshRequest_whenMultipleUsersReturnedFromPRD() throws Exception {
        GetRefreshUsersResponse getRefreshUsersResponse = TestDataBuilder.buildRefreshUsersResponse("1234");
        getRefreshUsersResponse.getUsers().add(new RefreshUser());
        doReturn(ResponseEntity.status(HttpStatus.OK).body(getRefreshUsersResponse))
            .when(prdFeignClient).getRefreshUsers(any());

        MvcResult result = mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders()))
            .andExpect(status().isInternalServerError())
            .andReturn();

        assertTrue(result.getResolvedException() instanceof ServiceException);
        assertEquals(String.format(EXPECTED_SINGLE_PRD_USER, "1234", "2"), result.getResolvedException().getMessage());
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        var s2SToken = MockUtils.generateDummyS2SToken(AUTHORISED_SERVICE);
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, "38a90097-434e-47ee-8ea1-9ea2a267f51d");
        return headers;
    }

}
