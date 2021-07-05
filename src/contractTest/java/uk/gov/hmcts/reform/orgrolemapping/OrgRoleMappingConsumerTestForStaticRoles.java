package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.google.common.collect.Maps;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import groovy.util.logging.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.junit.After;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.MessagingConfiguration;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicConsumer;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.junit.Assert.assertNotNull;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_getRoles")
@PactFolder("pacts")
@SpringBootTest
public class OrgRoleMappingConsumerTestForStaticRoles {

    private static final String RAS_GET_LIST_ROLES_URL = "/am/role-assignments/roles";

    @MockBean
    TopicConsumer topicConsumer;

    @Autowired
    DataSource dataSource;
    @MockBean
    TopicPublisher topicPublisher;

    @MockBean
    MessagingConfiguration messagingConfiguration;

    @MockBean
    ServiceBusSenderClient serviceBusSenderClient;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "am_roleAssignment_getRoles", consumer = "accessMgmt_orgRoleMapping")
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
                        .stringType(category, RoleCategory.LEGAL_OPERATIONS.name()))
                .object(role -> role
                        .stringType(name, "senior-tribunal-caseworker")
                        .stringType(label, "Senior Tribunal Caseworker")
                        .stringType(description, "Senior Tribunal caseworker")
                        .stringType(category, RoleCategory.LEGAL_OPERATIONS.name()))
                .object(role -> role
                        .stringType(name, "petitioner's-solicitor")
                        .stringType(label, "Petitioner's Solicitor")
                        .stringType(description, "Petitioner's Solicitor")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "respondent's-solicitor")
                        .stringType(label, "Respondent's Solicitor")
                        .stringType(description, "Respondent's Solicitor")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "barrister")
                        .stringType(label, "Barrister")
                        .stringType(description, "Barrister")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "cafcass-solicitor")
                        .stringType(label, "Cafcass Solicitor")
                        .stringType(description, "Cafcass Solicitor")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "external-professional-solicitor")
                        .stringType(label, "External Professional Solicitor")
                        .stringType(description, "External Professional Solicitorv")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "la-barrister")
                        .stringType(label, "LA Barrister")
                        .stringType(description, "LA Barrister")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "managing-local-authority")
                        .stringType(label, "Managing Local Authority")
                        .stringType(description, "Managing Local Authority")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "la-solicitor")
                        .stringType(label, "LA solicitor")
                        .stringType(description, "LA solicitor")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor")
                        .stringType(label, "Solicitor")
                        .stringType(description, "Solicitor")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-a")
                        .stringType(label, "Solicitor A")
                        .stringType(description, "Solicitor A")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-b")
                        .stringType(label, "Solicitor B")
                        .stringType(description, "Solicitor B")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-c")
                        .stringType(label, "Solicitor C")
                        .stringType(description, "Solicitor C")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-d")
                        .stringType(label, "Solicitor D")
                        .stringType(description, "Solicitor D")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-e")
                        .stringType(label, "Solicitor E")
                        .stringType(description, "Solicitor E")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-f")
                        .stringType(label, "Solicitor F")
                        .stringType(description, "Solicitor F")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-g")
                        .stringType(label, "Solicitor G")
                        .stringType(description, "Solicitor G")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-h")
                        .stringType(label, "Solicitor H")
                        .stringType(description, "Solicitor H")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-i")
                        .stringType(label, "Solicitor I")
                        .stringType(description, "Solicitor I")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "solicitor-j")
                        .stringType(label, "Solicitor J")
                        .stringType(description, "Solicitor J")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
                .object(role -> role
                        .stringType(name, "legal-representative")
                        .stringType(label, "Legal Representative")
                        .stringType(description, "Legal Representative")
                        .stringType(category, RoleCategory.PROFESSIONAL.name()))
        ).build();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @TestConfiguration
    static class Configuration {
        Connection connection;

        @Bean
        public EmbeddedPostgres embeddedPostgres() throws IOException {
            return EmbeddedPostgres
                    .builder()
                    .setPort(0)
                    .start();
        }

        @Bean
        public DataSource dataSource() throws IOException, SQLException {
            final EmbeddedPostgres pg = embeddedPostgres();

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);
            return new SingleConnectionDataSource(connection, true);
        }

        @PreDestroy
        public void contextDestroyed() throws IOException, SQLException {
            if (connection != null) {
                connection.close();
            }
            embeddedPostgres().close();
        }
    }
}
