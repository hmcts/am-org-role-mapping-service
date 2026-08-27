package uk.gov.hmcts.reform.orgrolemapping.controller;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JBSFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.JRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.RASFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.util.HashMap;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_RARB;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.MockUtils.S2S_XUI;
import static uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures.ACTOR_ID1;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator.PRD_USER_NOT_FOUND;

@TestPropertySource(properties = {
    "refresh.Job.authorisedServices=" + S2S_XUI
})
class RefreshControllerProfessionalRefreshIntegrationTest extends BaseAuthorisedTestIntegration {

    private static final String USER_ID = "1234";

    private static final String PROFESSIONAL_REFRESH_URL = "/am/role-mapping/professional/refresh";

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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/prm/insert_user_refresh_queue_138.sql"})
    public void shouldProcessProfessionalRefreshRequest() throws Exception {

        // GIVEN
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse(USER_ID)))
            .when(prdFeignClient).getRefreshUsers(USER_ID, null, null, null);

        // WHEN / THEN
        getRequestSpecification(S2S_RARB, ACTOR_ID1)
                .when().post(PROFESSIONAL_REFRESH_URL + "?userId=" + USER_ID)
                .then().assertThat()
                .statusCode(HttpStatus.OK.value())
                .and()
                .body(containsString(Constants.SUCCESS_ROLE_REFRESH));
    }

    @Test
    public void shouldRejectProfessionalRefreshRequest_withoutUserId() throws Exception {
        getRequestSpecification()
                .when().post(PROFESSIONAL_REFRESH_URL)
                .then().assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/prm/delete_user_refresh_queue.sql"})
    public void shouldErrorProfessionalRefreshRequest_whenNoAccessTypesInDB() throws Exception {

        // GIVEN
        doReturn(ResponseEntity.status(HttpStatus.OK).body(TestDataBuilder.buildGetRefreshUsersResponse(USER_ID)))
            .when(prdFeignClient).getRefreshUsers(USER_ID, null, null, null);

        // WHEN
        getRequestSpecification(S2S_RARB, ACTOR_ID1)
                .when().post(PROFESSIONAL_REFRESH_URL + "?userId=" + USER_ID)
                .then().assertThat()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    public void shouldErrorProfessionalRefreshRequest_whenNoPRDUserFound() throws Exception {

        // GIVEN
        Request request = Request.create(Request.HttpMethod.GET, "url", new HashMap<>(), null, new RequestTemplate());
        doThrow(new FeignException.NotFound("Not Found", request, null, null))
            .when(prdFeignClient).getRefreshUsers(USER_ID, null, null, null);

        // WHEN
        getRequestSpecification(S2S_RARB, ACTOR_ID1)
                .when().post(PROFESSIONAL_REFRESH_URL + "?userId=" + USER_ID)
                .then().assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .and()
                .body(containsString(String.format(Constants.RESOURCE_NOT_FOUND + " " + PRD_USER_NOT_FOUND, USER_ID)));
    }

    @Test
    public void shouldErrorProfessionalRefreshRequest_whenMultipleUsersReturnedFromPRD() throws Exception {

        // GIVEN
        GetRefreshUserResponse getRefreshUserResponse = TestDataBuilder.buildGetRefreshUsersResponse(USER_ID);
        getRefreshUserResponse.getUsers().add(new RefreshUser());
        doReturn(ResponseEntity.status(HttpStatus.OK).body(getRefreshUserResponse))
            .when(prdFeignClient).getRefreshUsers(USER_ID, null, null, null);

        // WHEN
        getRequestSpecification(S2S_RARB, ACTOR_ID1)
                .when().post(PROFESSIONAL_REFRESH_URL + "?userId=" + USER_ID)
                .then().assertThat()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

}
