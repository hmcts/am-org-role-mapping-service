package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

@RestController
@Slf4j
@NoArgsConstructor
@Hidden
public class WelcomeController {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;


    @Autowired
    public WelcomeController(
            BulkAssignmentOrchestrator bulkAssignmentOrchestrator) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
    }

    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        return "Welcome to Organisation Role Mapping Service";
    }
    //This is just a test API

    @PostMapping(
            path = "/am/role-mapping/staff/users",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "creates multiple role assignments based upon user profile mapping rules",
            security =
                    {
                            @SecurityRequirement(name = "Authorization"),
                            @SecurityRequirement(name = "ServiceAuthorization")
                    })
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = V1.Error.INVALID_REQUEST
    )
    public ResponseEntity<Object> createOrgMapping(@RequestBody UserRequest userRequest,
                                                   @RequestHeader(value = "userType")
                                                           UserType userType) {
        var startTime = System.currentTimeMillis();
        log.debug("createOrgMapping");
        log.info("Process has been Started for the userIds {}", userRequest.getUserIds());
        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest,
                userType);
        log.debug("Execution time of createOrgMapping() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(), startTime)));
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }


    //This method needed for the functional tests, so that RAS gets enough time to create records.
    @PostMapping(value = "/sleep")
    public ResponseEntity<String> waitFor(String duration) {
        return ResponseEntity.ok("Sleep time for Functional tests is over");

    }
}
