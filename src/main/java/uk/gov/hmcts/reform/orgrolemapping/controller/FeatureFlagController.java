package uk.gov.hmcts.reform.orgrolemapping.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
public class FeatureFlagController {

    @Autowired
    PersistenceService persistenceService;

    @Autowired
    PersistenceUtil persistenceUtil;

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
