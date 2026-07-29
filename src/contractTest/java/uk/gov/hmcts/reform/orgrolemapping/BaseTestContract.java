package uk.gov.hmcts.reform.orgrolemapping;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@ContextConfiguration(initializers = {BaseTestContract.WireMockServerInitializer.class})
@ActiveProfiles("ctest")
public abstract class BaseTestContract {

    public static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(options().dynamicPort());
    protected static final ObjectMapper mapper = new ObjectMapper();

    @Bean
    @Qualifier("jrdConsumer")
    ServiceBusReceiverClient jrdConsumer() {
        return Mockito.mock(ServiceBusReceiverClient.class);
    }

    @Bean
    @Qualifier("crdConsumer")
    ServiceBusReceiverClient crdConsumer() {
        return Mockito.mock(ServiceBusReceiverClient.class);
    }

    @Bean
    @Qualifier("getSubscriptionClient")
    ServiceBusReceiverClient getSubscriptionClient() {
        return Mockito.mock(ServiceBusReceiverClient.class);
    }

    @Bean
    @Qualifier("getSubscriptionClient1")
    ServiceBusReceiverClient getSubscriptionClient1() {
        return Mockito.mock(ServiceBusReceiverClient.class);
    }

    @Bean
    @Qualifier("clientRegistrationRepository")
    ClientRegistrationRepository getClientRegistrationRepository() {
        return Mockito.mock(ClientRegistrationRepository.class);
    }

    @Bean
    @Qualifier("reactiveClientRegistrationRepository")
    ReactiveClientRegistrationRepository getReactiveClientRegistrationRepository() {
        return Mockito.mock(ReactiveClientRegistrationRepository.class);
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

    static {
        if (!WIRE_MOCK_SERVER.isRunning()) {
            WIRE_MOCK_SERVER.start();
        }

        mapper.registerModule(new JavaTimeModule());
        mapper.setDefaultPropertyInclusion(
                JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.NON_NULL
                )
        );
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        // Force re-initialisation of base types for each test suite
    }
}
