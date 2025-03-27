package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.scheduler.Scheduler;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class PrmSchedulerController {

    private final Scheduler scheduler;


    @Autowired
    public PrmSchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @GetMapping(
            path = "/am/testing-support/prm/findAndUpdateCaseDefinitionChanges"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "PRM Process 1 findAndUpdateCaseDefinitionChanges",
            security =
            {
                @SecurityRequirement(name = AUTHORIZATION),
                @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = ProcessMonitorDto.class))
    )
    public ResponseEntity<Object> findAndUpdateCaseDefinitionChanges() {
        ProcessMonitorDto processMonitorDto = scheduler.findAndUpdateCaseDefinitionChanges();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

}
