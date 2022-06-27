package uk.gov.hmcts.reform.orgrolemapping.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                .flagName("iac_1_0")
                .serviceName("iac")
                .status(Boolean.TRUE)
                .build();

        FlagConfig flagConfig = persistenceUtil.convertFlagRequestToFlagConfig(flagRequest);
        assertNotNull(flagConfig);
        assertEquals("pr", flagConfig.getEnv());
        assertEquals("iac_1_0", flagConfig.getFlagName());
        assertEquals("iac", flagConfig.getServiceName());
        assertEquals(true, flagConfig.getStatus());

    }
}