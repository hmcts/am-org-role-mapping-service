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
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.util.ArrayList;
import java.util.List;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "referenceData_caseworkerRefUsers", port = "8991")
@ContextConfiguration(classes = {RefDataCaseworkerConsumerApplication.class})
@TestPropertySource(properties = {"feign.client.config.crdclient.url=http://localhost:8991"})
public class RefDataCaseworkerConsumerTest {

    @Autowired
    CRDFeignClient crdFeignClient;

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

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyCaseworkersFetch() {

        ResponseEntity<List<UserProfile>> caseWorkerProfiles =
            crdFeignClient.getCaseworkerDetailsById(buildUserRequest());
        assertThat(caseWorkerProfiles.getBody().get(0).getEmailId(), equalTo("sam.manuel@gmail.com"));

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
}
