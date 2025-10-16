package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.config.ProfessionalUserServiceConfig;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ForbiddenException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialRefreshRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.JudicialRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalRefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
public class RefreshController {

    @Autowired
    public RefreshController(RefreshOrchestrator refreshOrchestrator,
                             JudicialRefreshOrchestrator judicialRefreshOrchestrator,
                             ProfessionalRefreshOrchestrator professionalRefreshOrchestrator,
                             ProfessionalUserServiceConfig professionalUserServiceConfig) {
        this.refreshOrchestrator = refreshOrchestrator;
        this.judicialRefreshOrchestrator = judicialRefreshOrchestrator;
        this.professionalRefreshOrchestrator = professionalRefreshOrchestrator;
        this.professionalUserServiceConfig = professionalUserServiceConfig;
    }

    RefreshOrchestrator refreshOrchestrator;
    JudicialRefreshOrchestrator judicialRefreshOrchestrator;
    ProfessionalRefreshOrchestrator professionalRefreshOrchestrator;
    ProfessionalUserServiceConfig professionalUserServiceConfig;

    @PostMapping(
            path = "/am/role-mapping/refresh",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @Operation(summary = "refreshes caseworker role assignments",
            description = "operation can only be executed by services that are authorised to call the refresh "
                    + "controller otherwise an unauthorized service error will be returned",
            security =
            {
                @SecurityRequirement(name = AUTHORIZATION),
                @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ApiResponse(
            responseCode = "202",
            description = "Accepted",
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = V1.Error.INVALID_REQUEST,
            content = @Content()
    )
    @ApiResponse(
            responseCode = "403",
            description = V1.Error.UNAUTHORIZED_SERVICE,
            content = @Content()
    )
    @ApiResponse(
            responseCode = "422",
            description = V1.Error.UNPROCESSABLE_ENTITY_REQUEST_REJECTED,
            content = @Content()
    )
    public ResponseEntity<Object> refresh(@RequestParam Long jobId,
                                          @RequestBody(required = false) UserRequest userRequest) {
        refreshOrchestrator.validate(jobId, userRequest);
        refreshOrchestrator.refreshAsync(jobId, userRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping(
            path = "/am/role-mapping/judicial/refresh",
            produces = V1.MediaType.REFRESH_JUDICIAL_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "refreshes judicial role assignments",
            security =
            {
                @SecurityRequirement(name = AUTHORIZATION),
                @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ApiResponse(
            responseCode = "200",
            description = "Successful",
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = V1.Error.INVALID_REQUEST,
            content = @Content()
    )
    @ApiResponse(
            responseCode = "422",
            description = V1.Error.UNPROCESSABLE_ENTITY_REQUEST_REJECTED,
            content = @Content()
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

    @PostMapping(
        path = "/am/role-mapping/professional/refresh",
        produces = V1.MediaType.REFRESH_PROFESSIONAL_ASSIGNMENTS
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "refreshes professional role assignments",
        security =
            {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ApiResponse(
        responseCode = "200",
        description = "Successful",
        content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = V1.Error.INVALID_REQUEST,
        content = @Content()
    )
    @ApiResponse(
        responseCode = "404",
        description = Constants.RESOURCE_NOT_FOUND + " " + ProfessionalRefreshOrchestrator.PRD_USER_NOT_FOUND,
        content = @Content()
    )
    public ResponseEntity<Object> professionalRefresh(@RequestParam String userId) {
        if (!professionalUserServiceConfig.isRefreshApiEnabled()) {
            throw new ForbiddenException("PROFESSIONAL_REFRESH_API_ENABLED is false");
        }
        return professionalRefreshOrchestrator.refreshProfessionalUser(userId);
    }

}
