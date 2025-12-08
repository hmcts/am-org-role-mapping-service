package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import java.nio.charset.StandardCharsets;

@ContextConfiguration(initializers = {BaseTestIntegration.WireMockServerInitializer.class})
@ActiveProfiles("itest")
public abstract class BaseTestIntegration extends BaseTest {

    private static final String POSTGRES = "postgres";

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
            ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            final PostgreSQLContainer pg = new PostgreSQLContainer()
                    .withDatabaseName(POSTGRES);
            pg.start();

            TestPropertyValues.of(
                    "spring.datasource.url=" + pg.getJdbcUrl()
            ).applyTo(applicationContext.getEnvironment());
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
