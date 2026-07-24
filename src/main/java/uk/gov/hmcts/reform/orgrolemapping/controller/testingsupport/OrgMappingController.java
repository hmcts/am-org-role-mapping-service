package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
@NoArgsConstructor
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class OrgMappingController {

    public static final String CREATE_ORG_MAPPING_URI = "/am/testing-support/createOrgMapping";

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    @Autowired
    public OrgMappingController(
            BulkAssignmentOrchestrator bulkAssignmentOrchestrator) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
    }

    @PostMapping(
            path = CREATE_ORG_MAPPING_URI,
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "creates multiple role assignments based upon user profile mapping rules",
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
    public ResponseEntity<Object> createOrgMapping(@RequestBody UserRequest userRequest,
                                                   @RequestParam(value = "userType")
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


    /**
     * Create Org Mappings endpoint using old URL and header parameter.
     *
     * @deprecated Use {@link OrgMappingController#createOrgMapping(UserRequest, UserType)}
     */
    @SuppressWarnings({"squid:S1133", "DeprecatedIsStillUsed"})
    @Deprecated(forRemoval = true)
    @PostMapping(
            path = "/am/role-mapping/staff/users",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Hidden
    public ResponseEntity<Object> createOrgMappingDeprecated(@RequestBody UserRequest userRequest,
                                                             @RequestHeader(value = "userType")
                                                             UserType userType) {
        return createOrgMapping(userRequest, userType);
    }

}
