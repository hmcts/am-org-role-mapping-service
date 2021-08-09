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
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfilesResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInCaseWorkerProfile;
import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.convertInCaseWorkerProfileResponse;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_caseworkerRefUsers", port = "8991")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.crdclient.url=http://localhost:8991"})
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class RefDataCaseworkerConsumerTest {

    private static final String CRD_GET_USERS_BY_SERVICE = "/refdata/internal/staff/usersByServiceName";
    private static final String USERS_BY_SERVICE_QUERY = "ccd_service_names=IA&page_size=20&page_number=1&"
            + "sort_direction=ASC&sort_column=caseWorkerId";

    @Autowired
    CRDFeignClient crdFeignClient;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "referenceData_caseworkerRefUsers", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact generatePactFragment(PactDslWithProvider builder) throws JSONException,
        JsonProcessingException {

        return builder
            .given("A list of users for CRD request")
            .uponReceiving("A request for caseworkers")
            .path("/refdata/case-worker/users/fetchUsersById")
            .method("POST")
            .body(new ObjectMapper().writeValueAsString(buildUserRequest()))
            .willRespondWith()
            .status(200)
            .body(buildCaseworkerListResponsePactDsl())
            .toPact();
    }

    @Pact(provider = "referenceData_caseworkerRefUsers", consumer = "accessMgmt_orgRoleMapping")
    public RequestResponsePact getCaseworkersByServiceNamePact(PactDslWithProvider builder) throws JSONException {

        return builder
                .given("A list of staff profiles for CRD request by service names")
                .uponReceiving("A request for caseworkers by serviceName")
                .path(CRD_GET_USERS_BY_SERVICE)
                .query(USERS_BY_SERVICE_QUERY)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(buildCaseworkerListWithService())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyCaseworkersFetch() {
        ResponseEntity<List<Object>> response = null;
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();

        response = crdFeignClient.getCaseworkerDetailsById(buildUserRequest());
        Objects.requireNonNull(response.getBody()).forEach(o -> caseWorkerProfiles.add(convertInCaseWorkerProfile(o)));

        assertThat(caseWorkerProfiles.get(0).getEmailId(), equalTo("sam.manuel@gmail.com"));

    }

    @Test
    @PactTestFor(pactMethod = "getCaseworkersByServiceNamePact")
    public void verifyCaseworkersByServiceName() {
        ResponseEntity<List<CaseWorkerProfilesResponse>> caseWorkerProfiles =
                crdFeignClient.getCaseworkerDetailsByServiceName("IA",20,1,
                        "ASC", "caseWorkerId");

        assertThat(convertInCaseWorkerProfileResponse(caseWorkerProfiles.getBody().get(0)).getServiceName(), equalTo("IA"));

    }

    public static UserRequest buildUserRequest() {
        ArrayList<String> users = new ArrayList<>();
        users.add("userId1");
        users.add("userId2");
        return UserRequest.builder().userIds(users).build();
    }

    private DslPart buildCaseworkerListResponsePactDsl() {

        return newJsonArray(o -> {
            o.object(ob -> ob
                .stringType("first_name",
                    "Sam")
                .stringType("last_name",
                    "Manuel")
                .stringType("email_id", "sam.manuel@gmail.com")
                .numberType("region_id", 1)
                .stringType("region", "National")
                .numberType("user_type_id", 1)
                .stringType("user_type", "HMCTS")
                .stringMatcher("suspended", "true|false", "true")
                .minArrayLike("role", 1, r -> r
                    .stringType("role_id", "1")
                    .stringType("role", "senior-tribunal-caseworker")
                    .booleanType("is_primary", true)
                )
                .minArrayLike("base_location", 1, r -> r
                    .numberType("location_id", 1)
                    .stringType("location", "Aberdeen Tribunal Hearing Centre")
                    .booleanType("is_primary", true)
                )
                .minArrayLike("work_area", 1, r -> r
                    .stringType("area_of_work", "1")
                    .stringType("service_code", "BFA1")
                )
            );
        }).build();
    }

    private DslPart buildCaseworkerListWithService() {

        return newJsonArray(o -> {
            o.object(ob -> ob
                    .stringType("ccd_service_name", "IA")
                    .object("staff_profile", s -> s
                            .stringType("first_name","Sam")
                            .stringType("last_name","Manuel")
                            .stringType("email_id", "sam.manuel@gmail.com")
                            .numberType("region_id", 1)
                            .stringType("region", "National")
                            .stringType("user_type", "HMCTS")
                            .stringMatcher("suspended", "true|false", "true")
                            .numberType("user_type_id", 1)
                            .minArrayLike("role", 1, r -> r
                                    .stringType("role_id", "1")
                                    .stringType("role", "senior-tribunal-caseworker")
                                    .booleanType("is_primary", true)
                            )
                            .minArrayLike("work_area", 1, r -> r
                                    .stringType("service_code", "BFA1")
                            )
                    ));
        }).build();
    }
}
