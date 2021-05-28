package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

@RestController
@Slf4j
@NoArgsConstructor
public class RefreshController {

    @Autowired
    public RefreshController(RefreshOrchestrator refreshOrchestrator) {
        this.refreshOrchestrator = refreshOrchestrator;
    }

    RefreshOrchestrator refreshOrchestrator;

    @PostMapping(
            path = "/am/role-mapping/refresh",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.ACCEPTED)
    @ApiOperation("refreshes role assignments")
    @ApiResponses({
            @ApiResponse(
                    code = 202,
                    message = "Accepted",
                    response = Object.class
            ),
            @ApiResponse(
                    code = 400,
                    message = V1.Error.INVALID_REQUEST
            )
    })
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Object> refresh(@RequestParam Long jobId,
                                          @RequestBody(required = false) UserRequest userRequest) {

        return refreshOrchestrator.refresh(jobId, userRequest);

    }
}
