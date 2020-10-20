/*
package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.nio.charset.Charset;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


public class WelcomeControllerIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeControllerIntegrationTest.class);
    private static final String COUNT_RECORDS = "SELECT count(1) as n FROM role_assignment_request";
    private static final String GET_STATUS = "SELECT status FROM role_assignment_request where id = ?";
    private static final String REQUEST_ID = "21334a2b-79ce-44eb-9168-2d49a744be9c";

    private transient MockMvc mockMvc;

    @ClassRule
    public static WireMockRule roleAssignmentService = new WireMockRule(wireMockConfig().port(4096));

    private static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );

    @Autowired
    private transient WelcomeController welcomeController;

    @Before
    public void setUp() {
        this.mockMvc = standaloneSetup(this.welcomeController).build();

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
        UserRequest request = UserRequest.builder().users(Arrays.asList("21334a2b-79ce-44eb-9168-2d49a744be9c"))
                .build();
        logger.info(" createOrgRoleMappingTest...");
        String uri = "/am/role-mapping/staff/users";
        setRoleAssignmentWireMock(HttpStatus.CREATED);

        mockMvc.perform(post(uri)
                .contentType(JSON_CONTENT_TYPE)
                .headers(getHttpHeaders())
                .content(mapper.writeValueAsBytes(request))
        ).andExpect(status().is(201)).andReturn();
    }


    public void setRoleAssignmentWireMock(HttpStatus status) {
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
    }

    private HttpHeaders getHttpHeaders() {
        String authorisation = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIs";
        String serviceAuthorisation = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE2MDI2ODAwNjJ9.eTrBOVMQI4L";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authorisation);
        headers.set("ServiceAuthorization", "Bearer " + serviceAuthorisation);
        return headers;
    }
}
*/
