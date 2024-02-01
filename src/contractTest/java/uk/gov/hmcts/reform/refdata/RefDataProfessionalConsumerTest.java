package uk.gov.hmcts.reform.refdata;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_professionalInternalUsers", port = "8090")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.prdClient.url=http://localhost:8090"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class RefDataProfessionalConsumerTest {

    private static final String PRD_GET_REFRESH_USERS_URL = "/refdata/internal/v1/organisations/users";
    private static final String USER_IDENTIFIER = "b86aff5a-f12c-324d-849b-15aa4d86d6a7";
    private static final String SINCE = "2024-01-25T12:52:30.770894";
    private static final String SEARCH_AFTER = "03b98806-8473-42c6-a6e3-004364a58b44";
    private static final Integer PAGE_SIZE = 1;

    @Autowired
    PRDFeignClient prdFeignClient;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "referenceData_professionalInternalUsers", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getRefreshUserByUserIdentifier(PactDslWithProvider builder) {
        return builder
                .given("A user identifier for a PRD internal user request")
                .uponReceiving("A request for a professional user")
                .path(PRD_GET_REFRESH_USERS_URL)
                .query("userId=" + USER_IDENTIFIER)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(buildRefreshUserResponsePactDsl())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getRefreshUserByUserIdentifier")
    public void verifyGetRefreshUserByUserIdentifier() {
        ResponseEntity<Object> response = prdFeignClient
                .getRefreshUsers(null, USER_IDENTIFIER, null, null);

        assertNotNull(response);
    }

    @Pact(provider = "referenceData_professionalInternalUsers", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getRefreshUserBySinceSearchAfterAndPageSize(PactDslWithProvider builder) {
        return builder
                .given("A since, searchAfter & page size for a PRD internal user request")
                .uponReceiving("A request for professional users")
                .path(PRD_GET_REFRESH_USERS_URL)
                .query("since=" + SINCE
                        + "&searchAfter=" + SEARCH_AFTER
                        + "&pageSize=" + PAGE_SIZE
                )
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(buildRefreshUserResponsePactDsl())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getRefreshUserBySinceSearchAfterAndPageSize")
    public void verifyGetRefreshUserBySinceSearchAfterAndPageSize() {
        ResponseEntity<Object> response = prdFeignClient
                .getRefreshUsers(SINCE, null, PAGE_SIZE, SEARCH_AFTER);

        assertNotNull(response);
    }

    private DslPart buildRefreshUserResponsePactDsl() {
        return newJsonBody((o) -> {
            o.minArrayLike("users", 1, user -> user
                    .stringType("userIdentifier", "fba753f1-c194-3d30-bfe8-e80808da5a25")
                    .stringType("lastUpdated", "2024-01-25T12:52:30.798392")
                    .object("organisationInfo", orgInfo -> orgInfo
                            .stringType("organisationIdentifier", "0Z64OR3")
                            .stringType("status", "PENDING")
                            .stringType("lastUpdated", "2024-01-25T12:52:30.770894")
                            .array("organisationProfileIds", arr -> arr.stringType("SOLICITOR_PROFILE")))
                    .minArrayLike("userAccessTypes", 1, uat -> uat
                            .stringType("jurisdictionId", "CIVIL")
                            .stringType("organisationProfileId", "SOLICITOR_PROFILE")
                            .stringType("accessTypeId", "123")
                            .booleanType("enabled", true)
                    )
            );
            o.stringType("lastRecordInPage", "ab5b51f9-8c2e-46c8-979a-c204aef0c27b");
            o.booleanType("moreAvailable", false);
        }).build();
    }

}
