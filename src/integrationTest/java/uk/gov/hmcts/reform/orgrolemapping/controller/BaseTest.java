package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {
    protected static final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    TopicPublisher topicPublisher;

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Force re-initialisation of base types for each test suite
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
        public DataSource dataSource(@Autowired EmbeddedPostgres pg) throws Exception {

            final Properties props = new Properties();
            // Instruct JDBC to accept JSON string for JSONB
            props.setProperty("stringtype", "unspecified");
            connection = DriverManager.getConnection(pg.getJdbcUrl("postgres", "postgres"), props);
            DataSource datasource = new SingleConnectionDataSource(connection, true);
            return datasource;
        }



        @PreDestroy
        public void contextDestroyed() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
