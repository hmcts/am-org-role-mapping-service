package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.microsoft.azure.servicebus.SubscriptionClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.TestPropertySourceUtils;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@ContextConfiguration(initializers = {BaseTest.WireMockServerInitializer.class})
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@EnableConfigurationProperties
public abstract class BaseTest {

    public static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(options().dynamicPort());

    protected static final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd;

    @MockBean(name = "jrdConsumer")
    private SubscriptionClient jrdConsumer;

    @MockBean(name = "crdConsumer")
    private SubscriptionClient crdConsumer;

    @MockBean(name = "getSubscriptionClient")
    private SubscriptionClient getSubscriptionClient;

    @MockBean(name = "getSubscriptionClient1")
    private SubscriptionClient getSubscriptionClient1;

    @MockBean(name = "clientRegistrationRepository")
    private ClientRegistrationRepository getClientRegistrationRepository;

    @MockBean(name = "reactiveClientRegistrationRepository")
    private ReactiveClientRegistrationRepository getReactiveClientRegistrationRepository;

    static {
        if (!WIRE_MOCK_SERVER.isRunning()) {
            WIRE_MOCK_SERVER.start();
        }

        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // Force re-initialisation of base types for each test suite
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
