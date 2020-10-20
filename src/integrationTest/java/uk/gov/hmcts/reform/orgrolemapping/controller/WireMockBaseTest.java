/*
package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@DirtiesContext  // required for Jenkins agent
@AutoConfigureWireMock(port = 0)
public abstract class WireMockBaseTest extends BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(WireMockBaseTest.class);

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @Before
    public void initMock() throws IOException {
        final String hostUrl = "http://localhost:" + wiremockPort;

        LOG.info("Wire mock test, host url is {}", hostUrl);

        ReflectionTestUtils.setField("caseDefinitionHost", "caseDefinitionHost", hostUrl);
        ReflectionTestUtils.setField("uiDefinitionHost", "uiDefinitionHost", hostUrl);
        ReflectionTestUtils.setField("idamHost", "idamHost", hostUrl);
        ReflectionTestUtils.setField("userProfileHost", "userProfileHost", hostUrl);
        ReflectionTestUtils.setField("draftHost", "draftHost", hostUrl);
    }
}
*/
