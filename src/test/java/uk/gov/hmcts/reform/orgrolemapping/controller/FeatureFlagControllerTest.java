package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.util.PersistenceUtil;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagControllerTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private PersistenceUtil persistenceUtil;

    private FeatureFlagController sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new FeatureFlagController(persistenceService, persistenceUtil);
    }

    @Test
    void getFlagStatus() {

        String flagName = "iac_1_1";
        String env = "pr";

        Mockito.when(persistenceService.getStatusByParam(flagName, env)).thenReturn(Boolean.TRUE);
        ResponseEntity<Object> responseEntity = sut.getFeatureFlag(flagName, env);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue((Boolean) Objects.requireNonNull(responseEntity.getBody()));

    }

    @Test
    void getAllFeatureFlags() {

        Map<String, Boolean> allFlags = Map.of("iac_1_1", Boolean.TRUE, "iac_1_2", Boolean.FALSE);

        Mockito.when(persistenceService.getAllFeatureFlags()).thenReturn(allFlags);
        ResponseEntity<Map<String, Boolean>> responseEntity = sut.getAllFeatureFlags();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue((responseEntity.getBody() instanceof Map));
        Map<String, Boolean> bodyMap = (Map<String, Boolean>) responseEntity.getBody();
        assertEquals(allFlags.size(), bodyMap.size());
        assertTrue(bodyMap.get("iac_1_1"));
        assertFalse(bodyMap.get("iac_1_2"));
    }

    @Test
    void createFeatureFlag() {

        FlagRequest flagRequest = FlagRequest.builder()
                .env("pr")
                .flagName("iac_1_1")
                .serviceName("iac")
                .status(Boolean.TRUE)
                .build();

        FlagConfig flagConfig = FlagConfig.builder()
                .flagName(flagRequest.getFlagName())
                .env(flagRequest.getEnv())
                .serviceName(flagRequest.getServiceName())
                .status(flagRequest.getStatus())
                .build();

        Mockito.when(persistenceUtil.convertFlagRequestToFlagConfig(flagRequest)).thenReturn(flagConfig);
        Mockito.when(persistenceService.persistFlagConfig(flagConfig)).thenReturn(flagConfig);

        ResponseEntity<Object> responseEntity = sut.createFeatureFlag(flagRequest);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

    }

}
