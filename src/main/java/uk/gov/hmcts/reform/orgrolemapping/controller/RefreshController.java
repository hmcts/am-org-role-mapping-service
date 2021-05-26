package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RefreshOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import java.util.Optional;

@RestController
@Slf4j
@NoArgsConstructor
public class RefreshController {

    RefreshOrchestrator refreshOrchestrator;
    PersistenceService persistenceService;

    @Autowired
    public RefreshController(RefreshOrchestrator refreshOrchestrator, PersistenceService persistenceService) {
        this.refreshOrchestrator = refreshOrchestrator;
        this.persistenceService = persistenceService;
    }


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
    public ResponseEntity<Object> refresh(@RequestParam String jobId,
                                          @RequestBody(required = false) UserRequest userRequest) {

        return refreshOrchestrator.refresh(Long.parseLong(jobId), userRequest);

    }

    @PostMapping(
            path = "/am/test/job",
            consumes = {"application/json"}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Object> insertJob(
                                            @RequestParam String roleCategory,
                                            @RequestParam String jurisdiction,
                                            @RequestParam(required = false) Long jobId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Long linkedJobId,
                                            @RequestBody(required = false) UserRequest userRequest) {
        RefreshJobEntity newJob = RefreshJobEntity.builder()
                .jobId(jobId != null ? jobId : null)
                .jurisdiction(jurisdiction)
                .roleCategory(roleCategory)
                .status(status != null ? status : "NEW")
                .userIds(CollectionUtils.isNotEmpty(userRequest.getUserIds())
                        ? userRequest.getUserIds().toArray(new String[0]) : null)
                .linkedJobId(linkedJobId != null ? linkedJobId : null)
                .build();
        newJob = persistenceService.persistRefreshJob(newJob);
        return ResponseEntity.status(HttpStatus.OK).body(newJob);
    }

    @GetMapping(value = "/am/test/jobs/{jobId}")
    public ResponseEntity<Object>  fetchJob(@ApiParam(required = true)
                              @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        return ResponseEntity.status(HttpStatus.OK).body(refreshJobEntity.isPresent() ? refreshJobEntity
                .get() : null);
    }

    @DeleteMapping(value = "/am/test/jobs/{jobId}")
    public ResponseEntity<Object>  removeJob(@ApiParam(required = true)
                                            @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        if (refreshJobEntity.isPresent()) {
            persistenceService.deleteRefreshJob(refreshJobEntity.get());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
