package uk.gov.hmcts.reform.orgrolemapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTest;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@ContextConfiguration(initializers = {BaseTestContract.WireMockServerInitializer.class})
@ActiveProfiles("ctest")
public abstract class BaseTestContract extends BaseTest {

    public static class WireMockServerInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "wiremock.server.port=" + WIRE_MOCK_SERVER.port()
            );

            try {
                wiremockFixtures.stubIdamConfig();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            applicationContext.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
                if (WIRE_MOCK_SERVER.isRunning()) {
                    WIRE_MOCK_SERVER.shutdown();
                }
            });
        }
    }
}
