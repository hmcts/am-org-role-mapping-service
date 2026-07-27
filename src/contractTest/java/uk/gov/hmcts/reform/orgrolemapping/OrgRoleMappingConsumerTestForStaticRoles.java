package uk.gov.hmcts.reform.orgrolemapping;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import groovy.util.logging.Slf4j;
import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "am_roleAssignment_getRoles", pactVersion = PactSpecVersion.V1_1)
@PactFolder("pacts")
@TestPropertySource(properties = {
    "idam.api.url=http://localhost:5000",
    "spring.cache.type=simple"
})
public class OrgRoleMappingConsumerTestForStaticRoles extends BaseTestContract {

    @Autowired
    DataSource dataSource;

    @Bean
    JRDTopicPublisher jrdPublisher() {
        return Mockito.mock(JRDTopicPublisher.class);
    }

    @Bean
    CRDTopicPublisher crdPublisher() {
        return Mockito.mock(CRDTopicPublisher.class);
    }

    @Bean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient() {
        return Mockito.mock(ServiceBusSenderClient.class);
    }

    @Bean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd() {
        return Mockito.mock(ServiceBusSenderClient.class);
    }

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
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
        return new PactDslJsonArray()
                .object()
                .stringType(name, "judge")
                .stringType(label, "Judge - Sample role (Only for Testing)")
                .stringType(description, "Judicial office holder able to do judicial case work")
                .stringType(category, "JUDICIAL")
                .closeObject()
                .object()
                .stringType(name, "tribunal-caseworker")
                .stringType(label, "Tribunal Caseworker")
                .stringType(description, "Tribunal caseworker")
                .stringType(category, "LEGAL_OPERATIONS")
                .closeObject()
                .object()
                .stringType(name, "senior-tribunal-caseworker")
                .stringType(label, "Senior Tribunal Caseworker")
                .stringType(description, "Senior Tribunal caseworker")
                .stringType(category, "LEGAL_OPERATIONS")
                .closeObject();
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
                    .start();
        }

        @Bean
        public DataSource dataSource() throws IOException, SQLException {
            final EmbeddedPostgres pg = embeddedPostgres();

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            props.setProperty("user", "postgres");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres"), props);
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
