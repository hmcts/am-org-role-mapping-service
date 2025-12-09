package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ContextConfiguration(initializers = {BaseTestIntegration.WireMockServerInitializer.class})
@ActiveProfiles("itest")
public abstract class BaseTestIntegration extends BaseTest {

    private static final String POSTGRES = "postgres";
    private static final String POSTGRES_IMAGE = "postgres:latest";

    protected static final MediaType JSON_CONTENT_TYPE = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @MockBean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd;

    @TestConfiguration
    static class Configuration implements
            ApplicationContextInitializer<ConfigurableApplicationContext>, AfterAllCallback {

        private static final PostgreSQLContainer pg =
                new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
                .withDatabaseName(POSTGRES)
                .withStartupTimeout(Duration.ofSeconds(60))
                .withUsername(POSTGRES)
                .withPassword(POSTGRES);

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            pg.start();
        }

        @DynamicPropertySource
        static void registerPgProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", pg::getJdbcUrl);
            registry.add("spring.datasource.username", pg::getUsername);
            registry.add("spring.datasource.password", pg::getPassword);
        }

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            if (pg == null) {
                return;
            }
            pg.close();
        }
    }

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
