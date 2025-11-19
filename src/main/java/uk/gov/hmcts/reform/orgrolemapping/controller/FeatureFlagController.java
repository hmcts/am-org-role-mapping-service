package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.util.PersistenceUtil;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
@Hidden
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class FeatureFlagController {

    public static final String ALL_FEATURE_FLAG_STATUSES_URI = "/am/role-mapping/fetchAllFlagStatuses";

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

    @GetMapping(
        path = ALL_FEATURE_FLAG_STATUSES_URI,
        produces = V1.MediaType.MAP_ASSIGNMENTS,
        consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "get the statuses of all feature flags",
        security =
            {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = V1.Error.INVALID_REQUEST,
        content = @Content()
    )
    public ResponseEntity<Map<String, Boolean>> getAllFeatureFlags(
            @RequestParam(value = "env", required = false) String env) {
        return ResponseEntity.ok(persistenceService.getAllFeatureFlags(env));

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
