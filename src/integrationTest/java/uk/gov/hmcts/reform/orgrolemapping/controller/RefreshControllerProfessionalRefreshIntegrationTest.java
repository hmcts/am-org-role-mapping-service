package uk.gov.hmcts.reform.orgrolemapping.controller;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RequestMappingService;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;
import uk.gov.hmcts.reform.orgrolemapping.util.SecurityUtils;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.getHttpHeaders;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.EXPECTED_SINGLE_PRD_USER;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.NO_ACCESS_TYPES_FOUND;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.PRD_USER_NOT_FOUND;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=" + S2S_XUI
})
class RefreshControllerProfessionalRefreshIntegrationTest extends BaseTestIntegration {

    private static final String AUTHORISED_SERVICE = S2S_RARB;

    private static final String PROFESSIONAL_REFRESH_URL = "/am/role-mapping/professional/refresh";

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private PRDFeignClient prdFeignClient;

    @MockBean
    private CRDFeignClient crdFeignClient;

    @MockBean
    private JRDFeignClient jrdFeignClient;

    @MockBean
    private JBSFeignClient jbsFeignClient;

    @MockBean
    private RASFeignClient rasFeignClient;

    @MockBean
    private RequestMappingService<UserAccessProfile> requestMappingService;

    @MockBean
    private SecurityUtils securityUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;


    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        wiremockFixtures.resetRequests();
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_user_refresh_queue_138.sql"})
    public void shouldProcessProfessionalRefreshRequest() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("1234")))
            .when(prdFeignClient).getRefreshUsers(any());

        mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders(AUTHORISED_SERVICE)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.Message").value(containsString(Constants.SUCCESS_ROLE_REFRESH)))
            .andReturn();
    }

    @Test
    public void shouldRejectProfessionalRefreshRequest_withoutUserId() throws Exception {
        mockMvc.perform(post(PROFESSIONAL_REFRESH_URL)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders(S2S_XUI)))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/delete_user_refresh_queue.sql"})
    public void shouldErrorProfessionalRefreshRequest_whenNoAccessTypesInDB() throws Exception {
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse("1234")))
            .when(prdFeignClient).getRefreshUsers(any());
        MvcResult result = mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders(AUTHORISED_SERVICE)))
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
                .headers(getHttpHeaders(AUTHORISED_SERVICE)))
            .andExpect(status().isNotFound())
            .andReturn();
        assertTrue(result.getResolvedException() instanceof ResourceNotFoundException);
        assertEquals(String.format(Constants.RESOURCE_NOT_FOUND + " " + PRD_USER_NOT_FOUND, "1234"),
            result.getResolvedException().getMessage());
    }

    @Test
    public void shouldErrorProfessionalRefreshRequest_whenMultipleUsersReturnedFromPRD() throws Exception {
        GetRefreshUserResponse getRefreshUsersResponse = TestDataBuilder.buildGetRefreshUsersResponse("1234");
        getRefreshUsersResponse.getUsers().add(new RefreshUser());
        doReturn(ResponseEntity.status(HttpStatus.OK).body(getRefreshUsersResponse))
            .when(prdFeignClient).getRefreshUsers(any());
        MvcResult result = mockMvc.perform(post(PROFESSIONAL_REFRESH_URL + "?userId=1234")
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders(AUTHORISED_SERVICE)))
            .andExpect(status().isInternalServerError())
            .andReturn();
        assertTrue(result.getResolvedException() instanceof ServiceException);
        assertEquals(String.format(EXPECTED_SINGLE_PRD_USER, "1234", "2"), result.getResolvedException().getMessage());
    }

}
