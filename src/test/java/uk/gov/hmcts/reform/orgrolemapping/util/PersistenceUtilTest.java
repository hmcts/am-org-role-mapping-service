package uk.gov.hmcts.reform.orgrolemapping.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;

@RunWith(MockitoJUnitRunner.class)
class PersistenceUtilTest {

    @InjectMocks
    PersistenceUtil persistenceUtil = new PersistenceUtil();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void convertFlagRequestToFlagConfig() {
        FlagRequest flagRequest = FlagRequest.builder()
                .env("pr")
                .flagName("publiclaw_wa_1_0")
                .serviceName("publiclaw")
                .status(Boolean.TRUE)
                .build();

        FlagConfig flagConfig = persistenceUtil.convertFlagRequestToFlagConfig(flagRequest);
        assertNotNull(flagConfig);
        assertEquals("pr", flagConfig.getEnv());
        assertEquals("publiclaw_wa_1_0", flagConfig.getFlagName());
        assertEquals("publiclaw", flagConfig.getServiceName());
        assertEquals(true, flagConfig.getStatus());

    }
}