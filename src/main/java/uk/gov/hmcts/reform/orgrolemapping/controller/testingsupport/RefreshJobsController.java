package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Slf4j
@NoArgsConstructor
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class RefreshJobsController {

    private PersistenceService persistenceService;

    @Autowired
    public RefreshJobsController(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Operation(summary = "Create a new Refresh Job ", description = "To Create Refresh Job for actod/IDAM ID's "
            + "passed in the Body of the request and "
            + "roleCategory(JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL, CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC) "
            + "jurisdiction(Ex : CIVIL,IA,PRIVATELAW,PUBLICLAW) passed as Request Params"
            + "linkJob set to false if you want to refresh by service(default is TRUE)")
    @PostMapping(
            path = "/am/testing-support/job",
            consumes = {"application/json"}
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Object> insertJob(
            @RequestParam String roleCategory,
            @RequestParam String jurisdiction,
            @RequestParam(required = false, defaultValue = "true") Boolean linkJob,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long linkedJobId,
            @RequestParam(required = false) String comments,
            @RequestBody(required = false) UserRequest userRequest) {
        RefreshJobEntity newJob = RefreshJobEntity.builder()
                .jobId(jobId)
                .jurisdiction(jurisdiction)
                .roleCategory(roleCategory)
                .status(status != null ? status : "NEW")
                .userIds(CollectionUtils.isNotEmpty(userRequest.getUserIds())
                        ? userRequest.getUserIds().toArray(new String[0]) : null)
                .linkedJobId(linkedJobId)
                .comments(comments)
                .build();
        newJob = persistenceService.persistRefreshJob(newJob);
        if (Boolean.TRUE.equals(linkJob)) {
            newJob.setLinkedJobId(newJob.getJobId());
            newJob = persistenceService.persistRefreshJob(newJob);
        }
        return ResponseEntity.status(HttpStatus.OK).body(newJob);
    }

    @GetMapping(value = "/am/testing-support/jobs/{jobId}")
    public ResponseEntity<Object>  fetchJob(@Parameter(required = true)
                                            @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        return ResponseEntity.status(HttpStatus.OK).body(refreshJobEntity.isPresent() ? refreshJobEntity
                .get() : "Job Id not Found");
    }

    @DeleteMapping(value = "/am/testing-support/jobs/{jobId}")
    public ResponseEntity<Object>  removeJob(@Parameter(required = true)
                                             @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        if (refreshJobEntity.isPresent()) {
            persistenceService.deleteRefreshJob(refreshJobEntity.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
