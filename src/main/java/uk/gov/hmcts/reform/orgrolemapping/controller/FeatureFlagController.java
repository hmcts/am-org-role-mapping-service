package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.util.PersistenceUtil;

@RestController
@Hidden
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class FeatureFlagController {

    private final PersistenceService persistenceService;
    private final PersistenceUtil persistenceUtil;

    @Autowired
    public FeatureFlagController(PersistenceService persistenceService,
                                 PersistenceUtil persistenceUtil) {
        this.persistenceService = persistenceService;
        this.persistenceUtil = persistenceUtil;
    }


    @GetMapping(value = "/am/role-mapping/fetchFlagStatus")
    public ResponseEntity<Object> getFeatureFlag(@RequestParam(value = "flagName") String flagName,
                                                 @RequestParam(value = "env", required = false) String env) {
        return ResponseEntity.ok(persistenceService.getStatusByParam(flagName, env));

    }

    @PostMapping(
            path = "/am/role-mapping/createFeatureFlag",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {"application/json"}
    )
    public ResponseEntity<Object> createFeatureFlag(@RequestBody() FlagRequest flagRequest) {

        var flagConfig = persistenceUtil.convertFlagRequestToFlagConfig(flagRequest);
        return ResponseEntity.ok(persistenceService.persistFlagConfig(flagConfig));

    }

}
