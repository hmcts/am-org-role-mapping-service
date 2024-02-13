package uk.gov.hmcts.reform.refdata;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_professionalInternalUsersV2", port = "8090")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.prdClient.url=http://localhost:8090"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class RefDataProfessionalUserInternalConsumerTestV2 {

    private static final String PRD_GET_USERS_IN_ORGS_URL
            = "/refdata/internal/v2/organisations/users";
    private static final String SEARCH_AFTER_ORG = "64a7a462-33b6-49a6-9efd-39220d0ea0a8";
    private static final String SEARCH_AFTER_USER = "87cdc47e-97bd-4d68-ae14-10b152fc2e6a";
    private static final Integer PAGE_SIZE = 1;
    private static final boolean SHOW_DELETED = true;

    @Autowired
    PRDFeignClient prdFeignClient;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }

    @Disabled
    @Test
    @PactTestFor(pactMethod = "getUsersByOrganisationIdentifiersWithPageSizeAndSearchAfter")
    public void verifyGetUsersByOrganisationIdentifiersWithPageSizeAndSearchAfter() {
        ResponseEntity<UsersByOrganisationResponse> response = prdFeignClient
                .getUsersByOrganisation(PAGE_SIZE, SEARCH_AFTER_ORG, SEARCH_AFTER_USER, SHOW_DELETED,
                        createRequestBody());

        assertNotNull(response);
    }

    @Pact(provider = "referenceData_professionalInternalUsersV2", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getUsersByOrganisationIdentifiersWithPageSizeAndSearchAfter(PactDslWithProvider builder)
            throws JsonProcessingException {
        return builder
                .given("A page size, list of organisation identifiers and search after for a PRD internal " +
                        "user request")
                .uponReceiving("A request for organisations")
                .path(PRD_GET_USERS_IN_ORGS_URL)
                .query("&pageSize=" + PAGE_SIZE
                        + "&searchAfterOrg=" + SEARCH_AFTER_ORG
                        + "&searchAfterUser=" + SEARCH_AFTER_USER
                        + "&showDeleted=" + SHOW_DELETED)
                .body(new ObjectMapper().writeValueAsString(
                        createRequestBody()
                ))
                .method("POST")
                .willRespondWith()
                .status(200)
                .body(buildOrganisationUsersResponsePactDsl())
                .toPact();
    }

    @Disabled
    @Test
    @PactTestFor(pactMethod = "getUsersByOrganisationIdentifiersWithPageSizeAndNoSearchAfter")
    public void verifyGetUsersByOrganisationIdentifiersWithPageSizeAndNoSearchAfter() {
        ResponseEntity<UsersByOrganisationResponse> response = prdFeignClient
                .getUsersByOrganisation(PAGE_SIZE, null, null, SHOW_DELETED,
                        createRequestBody());

        assertNotNull(response);
    }

    @Pact(provider = "referenceData_professionalInternalUsersV2", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getUsersByOrganisationIdentifiersWithPageSizeAndNoSearchAfter(PactDslWithProvider builder)
            throws JsonProcessingException {
        return builder
                .given("A page size, list of organisation identifiers and no search after for a PRD internal " +
                        "user request")
                .uponReceiving("A request for organisations")
                .path(PRD_GET_USERS_IN_ORGS_URL)
                .query("&pageSize=" + PAGE_SIZE+ "&showDeleted=" + SHOW_DELETED)
                .body(new ObjectMapper().writeValueAsString(
                        createRequestBody()
                ))
                .method("POST")
                .willRespondWith()
                .status(200)
                .body(buildOrganisationUsersResponsePactDsl())
                .toPact();
    }

    private static UsersByOrganisationRequest createRequestBody() {
        return UsersByOrganisationRequest.builder()
                .organisationIdentifiers(List.of("ABC12345"))
                .build();
    }

    private DslPart buildOrganisationUsersResponsePactDsl() {
        return newJsonBody((o) -> {
            o.minArrayLike("organisationInfo", 1, orgInfo -> orgInfo
                    .stringType("organisationIdentifier", "0Z64OR3")
                    .stringType("status", "PENDING")
                    .array("organisationProfileIds", arr -> arr.stringType("SOLICITOR_PROFILE"))
                    .minArrayLike("users", 1, user -> user
                            .stringType("userIdentifier", "0Z64OR3")
                            .stringType("firstName", "John")
                            .stringType("lastName", "Smith")
                            .stringType("email", "test@test.com")
                            .stringType("lastUpdated", "2020-10-20T10:00:00")
                            .minArrayLike("userAccessTypes", 1, userAccessType -> userAccessType
                                    .stringType("jurisdictionId", "SSCS")
                                    .stringType("organisationProfileId", "SOLICITOR")
                                    .stringType("accessTypeId", "1")
                                    .booleanType("enabled", true)))
            );
            o.stringType("lastOrgInPage", "ab5b51f9-8c2e-46c8-979a-c204aef0c27b");
            o.stringType("lastUserInPage", "ab5b51f9-8c2e-46c8-979a-c204aef0c27b");
            o.booleanType("moreAvailable", false);
        }).build();
    }
}
