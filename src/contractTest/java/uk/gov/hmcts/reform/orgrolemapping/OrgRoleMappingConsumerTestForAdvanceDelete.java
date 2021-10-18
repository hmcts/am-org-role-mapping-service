package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDMessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_deleteAssignment")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForAdvanceDelete {

    private static final String RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL = "/am/role-assignments/query";
    private static final String RAS_ADVANCE_DELETE = RAS_SEARCH_QUERY_ROLE_ASSIGNMENT_URL + "/delete";
    private static final String ACTOR_ID_ADV = "14a21569-eb80-4681-b62c-6ae2ed069e5f";
    private static final String TRIBUNAL_CASEWORKER = "tribunal-caseworker";
    public static final String SERVICE = "application/vnd.uk.gov.hmcts.role-assignment-service";
    public static final String DELETE_ASSIGNMENTS = SERVICE
            + ".post-assignments-delete-request+json;charset=UTF-8;version=1.0";

    @MockBean
    JRDTopicConsumer crdConsumer;
    @MockBean
    CRDTopicConsumer jrdConsumer;

    @MockBean
    JRDTopicPublisher jrdPublisher;
    @MockBean
    CRDTopicPublisher crdPublisher;

    @MockBean
    JRDMessagingConfiguration jrdMessagingConfiguration;

    @MockBean
    CRDMessagingConfiguration crdMessagingConfiguration;

    @MockBean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    private String createRoleAssignmentRequestAdvanceDelete() {

        return "{\"queryRequests\":[{\"actorId\":[\"14a21569-eb80-4681-b62c-6ae2ed069e5f\"]},"
                + "{\"roleName\": [\"tribunal-caseworker\"]},"
                + "{\"roleType\": [\"CASE\"]},"
                + "{\"attributes\": {"
                + "\"caseId\": [\"1111222233334444\"]}}"
                + "]}";
    }

    @Pact(provider = "am_roleAssignment_deleteAssignment", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact executeAdvanceDeleteAndGet200(PactDslWithProvider builder) {

        return builder
                .given("Delete the set of selected role assignments as per given delete by query request")
                .uponReceiving(
                        "Delete the set of selected role assignments as per given delete by query request")
                .path(RAS_ADVANCE_DELETE)
                .method(HttpMethod.POST.toString())
                .body(createRoleAssignmentRequestAdvanceDelete(), String.valueOf(ContentType.JSON))
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeAdvanceDeleteAndGet200")
    void advancedDeleteAndGet200Test(MockServer mockServer)
            throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createRoleAssignmentRequestAdvanceDelete())
                        .post(mockServer.getUrl() + RAS_ADVANCE_DELETE)
                        .then()
                        .statusCode(200)
                        .log().all().extract().asString();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

}
