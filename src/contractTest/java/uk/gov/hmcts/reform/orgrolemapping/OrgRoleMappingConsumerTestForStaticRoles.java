package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
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

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.junit.Assert.assertNotNull;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_role_assignment_service_roles")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForStaticRoles {

    private static final String RAS_GET_LIST_ROLES_URL = "/am/role-assignments/roles";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_role_assignment_service_roles", consumer = "am_org_role_mapping")
    public RequestResponsePact executeGetListOfRolesAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A list of roles are available in role assignment service")
                .uponReceiving("RAS takes s2s/auth token and returns list of roles")
                .path(RAS_GET_LIST_ROLES_URL)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createRolesResponse())
                .toPact();
    }


    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.get-roles+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    @NotNull
    private Map<String, String> getRoleAssignmentResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments"
               + "+json");
        return responseHeaders;
    }

    @Test
    @PactTestFor(pactMethod = "executeGetListOfRolesAndGet200")
    void getListOfRolesAndGet200Test(MockServer mockServer) {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .get(mockServer.getUrl() + RAS_GET_LIST_ROLES_URL)
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }

    private DslPart createRolesResponse() {
        String name = "name";
        String label = "label";
        String description = "description";
        String category = "category";
        return newJsonArray(o -> o
                .object(role -> role
                        .stringType(name, "judge")
                        .stringType(label, "Judge - Sample role (Only for Testing)")
                        .stringType(description, "Judicial office holder able to do judicial case work")
                        .stringType(category, "JUDICIAL"))
                .object(role -> role
                        .stringType(name, "tribunal-caseworker")
                        .stringType(label, "Tribunal Caseworker")
                        .stringType(description, "Tribunal caseworker")
                        .stringType(category, "LEGAL_OPERATIONS"))
                .object(role -> role
                        .stringType(name, "senior-tribunal-caseworker")
                        .stringType(label, "Senior Tribunal Caseworker")
                        .stringType(description, "Senior Tribunal caseworker")
                        .stringType(category, "LEGAL_OPERATIONS"))
        ).build();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }
}
