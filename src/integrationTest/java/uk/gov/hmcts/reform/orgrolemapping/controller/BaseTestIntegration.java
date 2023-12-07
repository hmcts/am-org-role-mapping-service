package uk.gov.hmcts.reform.orgrolemapping.controller;

import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("itest")
public abstract class BaseTestIntegration extends BaseTest {

    @MockBean
    @Qualifier("crdPublisher")
    ServiceBusSenderClient serviceBusSenderClient;

    @MockBean
    @Qualifier("jrdPublisher")
    ServiceBusSenderClient serviceBusSenderClientJrd;
}
