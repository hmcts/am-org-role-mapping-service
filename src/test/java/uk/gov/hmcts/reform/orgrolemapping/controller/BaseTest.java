package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
public abstract class BaseTest {

    public static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(options().dynamicPort());

    protected static final ObjectMapper mapper = new ObjectMapper();

    @MockBean(name = "jrdConsumer")
    private ServiceBusReceiverClient jrdConsumer;

    @MockBean(name = "crdConsumer")
    private ServiceBusReceiverClient crdConsumer;

    @MockBean(name = "getSubscriptionClient")
    private ServiceBusReceiverClient getSubscriptionClient;

    @MockBean(name = "getSubscriptionClient1")
    private ServiceBusReceiverClient getSubscriptionClient1;

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
}
