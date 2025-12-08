package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import groovy.util.logging.Slf4j;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

import javax.sql.DataSource;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_getRoles")
@PactFolder("pacts")
public class OrgRoleMappingConsumerTestForStaticRoles extends BaseTestContract {

    private static final String POSTGRES = "postgres";

    @Autowired
    DataSource dataSource;

    @MockBean
    JRDTopicPublisher jrdPublisher;
    @MockBean
    CRDTopicPublisher crdPublisher;

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

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Map.of("Content-Type",
                "application/vnd.uk.gov.hmcts.role-assignment-service.get-roles+json;charset=UTF-8;version=1.0");
        return responseHeaders;
    }

    @NotNull
    private Map<String, String> getRoleAssignmentResponseHeaders() {
        Map<String, String> responseHeaders =
                Map.of("Content-Type", "application/vnd.uk.gov.hmcts.role-assignment-service.create-assignments"
                + "+json");
        return responseHeaders;
    }

    private DslPart createRolesResponse() {
        var name = "name";
        var label = "label";
        var description = "description";
        var category = "category";
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

    @TestConfiguration
    static class Configuration implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            final PostgreSQLContainer pg = new PostgreSQLContainer()
                    .withDatabaseName(POSTGRES)
                    .withUsername(POSTGRES)
                    .withPassword(POSTGRES);
            pg.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + pg.getJdbcUrl(),
                    "spring.datasource.username=" + pg.getUsername(),
                    "spring.datasource.password=" + pg.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
