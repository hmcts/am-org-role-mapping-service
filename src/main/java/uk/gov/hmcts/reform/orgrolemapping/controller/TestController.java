package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Hidden;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;

import java.util.Optional;

@RestController
@Hidden
public class TestController {

    PersistenceService persistenceService;

    @Autowired
    public TestController(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
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
    public ResponseEntity<Object> fetchJob(@Parameter(required = true)
                                            @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        return ResponseEntity.status(HttpStatus.OK).body(refreshJobEntity.isPresent() ? refreshJobEntity
                .get() : null);
    }

    @DeleteMapping(value = "/am/test/jobs/{jobId}")
    public ResponseEntity<Object> removeJob(@Parameter(required = true)
                                             @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        if (refreshJobEntity.isPresent()) {
            persistenceService.deleteRefreshJob(refreshJobEntity.get());
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
