package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.CoreMatchers.equalTo;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_role_assignment_service")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTest {

    private static final String RAS_GET_LIST_ROLES_URL = "/am/role-assignments/roles";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetListOfRolesAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of roles are available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns list of roles")
                .path(RAS_GET_LIST_ROLES_URL)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createResponse())
                .toPact();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.get-roles+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    @Test
    @PactTestFor(pactMethod = "executeGetListOfRolesAndGet200")
    void getListOfRolesAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .get(mockServer.getUrl() + RAS_GET_LIST_ROLES_URL)
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        JSONObject role = jsonArray.getJSONObject(0);
        assertThat(role.get("name"), equalTo("judge"));
        assertThat(role.get("description"), equalTo("Judicial office holder able to do judicial case work"));
        assertThat(role.get("label"), equalTo("Judge - Sample role (Only for Testing)"));
        assertThat(role.get("category"), equalTo("JUDICIAL"));
    }


    private String createResponse() {
        String response = "";
        response = "[\n"
                + "  {\n"
                + "    \"name\": \"judge\",\n"
                + "    \"label\": \"Judge - Sample role (Only for Testing)\",\n"
                + "    \"description\": \"Judicial office holder able to do judicial case work\",\n"
                + "    \"category\": \"JUDICIAL\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"name\": \"tribunal-caseworker\",\n"
                + "    \"label\": \"Tribunal Caseworker\",\n"
                + "    \"description\": \"Tribunal caseworker\",\n"
                + "    \"category\": \"STAFF\"\n"
                + "  },\n"
                + "  {\n"
                + "    \"name\": \"senior-tribunal-caseworker\",\n"
                + "    \"label\": \"Senior Tribunal Caseworker\",\n"
                + "    \"description\": \"Senior Tribunal caseworker\",\n"
                + "    \"category\": \"STAFF\"\n"
                + "  }\n"
                + "]";
        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
