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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationByProfileIdsResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationsResponse;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.PRDFeignClient;

import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_organisationalInternal", port = "8090")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.prdClient.url=http://localhost:8090"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class RefDataProfessionalOrganisationalInternalConsumerTest {

    private static final String PRD_GET_ORG_BY_PROFILE_URL
            = "/refdata/internal/v1/organisations/getOrganisationsByProfile";
    private static final String SEARCH_AFTER = "03b98806-8473-42c6-a6e3-004364a58b44";
    private static final Integer PAGE_SIZE = 1;
    private static final String PRD_RETREIVE_ORGANISATIONS = "/refdata/internal/v1/organisations";
    private static final String LAST_UPDATED_SINCE = "2023-12-31T12:34:56";

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
    @Pact(provider = "referenceData_organisationalInternal", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getOrganisationsByProfileIdsWithPageSize(PactDslWithProvider builder)
            throws JsonProcessingException {
        return builder
                .given("A page size & list of organisation profiles for a PRD internal organisation request")
                .uponReceiving("A request for organisations")
                .path(PRD_GET_ORG_BY_PROFILE_URL)
                .query("&pageSize=" + PAGE_SIZE)
                .body(new ObjectMapper().writeValueAsString(
                        OrganisationByProfileIdsRequest.builder()
                                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                                .build()
                        ))
                .method("POST")
                .willRespondWith()
                .status(200)
                .body(buildOrganisationResponsePactDsl())
                .toPact();
    }

    @Disabled
    @Test
    @PactTestFor(pactMethod = "getOrganisationsByProfileIdsWithPageSize")
    public void verifyGetOrganisationsByProfileIdsWithPageSize() {
        ResponseEntity<OrganisationByProfileIdsResponse> response = prdFeignClient
                .getOrganisationsByProfileIds(PAGE_SIZE, null, new OrganisationByProfileIdsRequest(
                        List.of("SOLICITOR_PROFILE")
                ));

        assertNotNull(response);
    }

    @Disabled
    @Pact(provider = "referenceData_organisationalInternal", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getOrganisationsByProfileIdsWithPageSizeAndSearchAfter(PactDslWithProvider builder)
            throws JsonProcessingException {
        return builder
                .given("A page size, search after & list of organisation profiles for a "
                        + "PRD internal organisation request")
                .uponReceiving("A request for organisations")
                .path(PRD_GET_ORG_BY_PROFILE_URL)
                .query("&pageSize=" + PAGE_SIZE
                        + "&searchAfter=" + SEARCH_AFTER
                )
                .body(new ObjectMapper().writeValueAsString(
                        OrganisationByProfileIdsRequest.builder()
                                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                                .build()
                ))
                .method("POST")
                .willRespondWith()
                .status(200)
                .body(buildOrganisationResponsePactDsl())
                .toPact();
    }

    @Disabled
    @Test
    @PactTestFor(pactMethod = "getOrganisationsByProfileIdsWithPageSizeAndSearchAfter")
    public void verifyGetOrganisationsByProfileIdsWithPageSizeAndSearchAfter() {
        ResponseEntity<OrganisationByProfileIdsResponse> response = prdFeignClient
                .getOrganisationsByProfileIds(PAGE_SIZE, SEARCH_AFTER, new OrganisationByProfileIdsRequest(
                        List.of("SOLICITOR_PROFILE")
                ));

        assertNotNull(response);
    }

    @Disabled
    @Pact(provider = "referenceData_organisationalInternal", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact retrieveOrganisations(PactDslWithProvider builder)
            throws JsonProcessingException {
        return builder
                .given("Active organisations exists for a logged in user using lastUpdatedSince")
                .uponReceiving("A request for organisations")
                .path(PRD_RETREIVE_ORGANISATIONS)
                .query("&since=" + LAST_UPDATED_SINCE
                        + "&size=" + PAGE_SIZE
                        + "&page=" + 1
                )
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(buildOrganisationProfileResponsePactDsl())
                .toPact();
    }

    //@TODO temp disabled during PR build - remove at master
    @Disabled
    @Test
    @PactTestFor(pactMethod = "retrieveOrganisations")
    public void verifyRetrieveOrganisations() {
        ResponseEntity<OrganisationsResponse> response = prdFeignClient
                .retrieveOrganisations(null, LAST_UPDATED_SINCE, null, 1, PAGE_SIZE);

        assertNotNull(response);
    }

    private DslPart buildOrganisationResponsePactDsl() {
        return newJsonBody((o) -> {
            o.minArrayLike("organisationInfo", 1, orgInfo -> orgInfo
                    .stringType("organisationIdentifier", "0Z64OR3")
                    .stringType("status", "PENDING")
                    .stringType("lastUpdated", "2024-01-25T12:52:30.770894")
                    .array("organisationProfileIds", arr -> arr.stringType("SOLICITOR_PROFILE")));
            o.stringType("lastRecordInPage", "ab5b51f9-8c2e-46c8-979a-c204aef0c27b");
            o.booleanType("moreAvailable", false);
        }).build();
    }

    private DslPart buildOrganisationProfileResponsePactDsl() {
        return newJsonBody((o) -> {
            o.minArrayLike("organisations", 1, orgInfo -> orgInfo
                    .stringType("organisationIdentifier", "0Z64OR3")
                    .stringType("status", "PENDING")
                    .stringType("lastUpdated", "2024-01-01T12:34:56.789012")
                    .array("organisationProfileIds", arr -> arr.stringType("SOLICITOR_PROFILE")));
            o.booleanType("moreAvailable", false);
        }).build();
    }
}
