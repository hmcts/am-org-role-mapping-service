package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

@RestController
@Slf4j
public class RefreshController {

    @Autowired
    public RefreshController(RefreshOrchestrator refreshOrchestrator,
                             JudicialRefreshOrchestrator judicialRefreshOrchestrator) {
        this.refreshOrchestrator = refreshOrchestrator;
        this.judicialRefreshOrchestrator = judicialRefreshOrchestrator;
    }

    RefreshOrchestrator refreshOrchestrator;

    JudicialRefreshOrchestrator judicialRefreshOrchestrator;

    @PostMapping(
            path = "/am/role-mapping/refresh",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ApiResponse(
            responseCode = "202",
            description = "Accepted",
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = V1.Error.INVALID_REQUEST
    )
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Object> refresh(@RequestParam Long jobId,
                                          @RequestBody(required = false) UserRequest userRequest) {
        refreshOrchestrator.validate(jobId,userRequest);
        return refreshOrchestrator.refresh(jobId, userRequest);

    }


    @PostMapping(
            path = "/am/role-mapping/judicial/refresh",
            produces = V1.MediaType.REFRESH_JUDICIAL_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "refreshes judicial role assignments")
    @ApiResponse(
            responseCode = "200",
            description = "Successful",
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = V1.Error.INVALID_REQUEST
    )
    @ApiResponse(
            responseCode = "422",
            description = V1.Error.UNPROCESSABLE_ENTITY_REQUEST_REJECTED
    )
    public ResponseEntity<Object> judicialRefresh(@RequestHeader(value = "x-correlation-id", required = false)
                                                              String correlationId,
                                                  @Validated @NonNull @RequestBody
                                                              JudicialRefreshRequest judicialRefreshRequest) {
        if (!StringUtils.isEmpty(correlationId)) {
            ValidationUtil.validateId(Constants.UUID_PATTERN, correlationId);
        }
        return judicialRefreshOrchestrator.judicialRefresh(judicialRefreshRequest.getRefreshRequest());
    }
}
