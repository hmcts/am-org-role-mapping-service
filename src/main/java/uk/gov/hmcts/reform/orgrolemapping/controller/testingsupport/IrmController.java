package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.scheduler.IrmScheduler;

import java.util.Arrays;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
@NoArgsConstructor
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class IrmController {

    private IrmScheduler irmScheduler;
    private IdamRoleMappingService idamRoleMappingService;

    @Autowired
    public IrmController(IrmScheduler irmScheduler, IdamRoleMappingService idamRoleMappingService) {
        this.irmScheduler = irmScheduler;
        this.idamRoleMappingService = idamRoleMappingService;
    }

    @GetMapping(
        path = "/am/testing-support/irm/processJudicialQueue"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "IRM Process Judicial Queue",
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
    public ResponseEntity<Object> processJudicialQueue() {
        ProcessMonitorDto processMonitorDto = irmScheduler.processJudicialQueue();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

    @GetMapping(
        path = "/am/testing-support/irm/user"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "IRM Update User",
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
    public ResponseEntity<Object> updateUser(
            @Parameter(description = "UserId")
            @RequestParam String userId) {
        ProcessMonitorDto processMonitorDto = idamRoleMappingService.updateUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

    @GetMapping(
        path = "/am/testing-support/irm/user/invite"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "IRM Invite User",
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
    public ResponseEntity<Object> inviteUser(
            @Parameter(description = "UserId")
            @RequestParam String userId,
            @Parameter(description = "RoleNames")
            @RequestParam String[] roleNames) {
        ProcessMonitorDto processMonitorDto = idamRoleMappingService.inviteUser(userId,
                Arrays.stream(roleNames).toList());
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }
}
