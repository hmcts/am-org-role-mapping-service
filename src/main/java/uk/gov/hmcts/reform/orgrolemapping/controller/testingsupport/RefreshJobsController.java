package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.RefreshJob;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.PersistenceService;

import java.time.ZonedDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;

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

    @Operation(summary = "Create a new Refresh Job ")
    @PostMapping(
            path = "/am/testing-support/job",
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = { @Content(schema = @Schema(implementation = RefreshJob.class), mediaType = "application/json") }
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<RefreshJob> insertJob(
            @Parameter(description
                    = "The Role Category: one of `JUDICIAL` or `LEGAL_OPERATIONS` for a caseworker refresh.")
            @RequestParam String roleCategory,
            @Parameter(description = "The Jurisdiction / Service Name to use in a search based refresh.")
            @RequestParam String jurisdiction,
            @Parameter(description
                = "`true` will auto assign the `linkedJobId` value ready for a targeted refresh of users.  "
                + "Set to `false` if you want to refresh all users in the given jurisdiction."
            )
            @RequestParam(required = false, defaultValue = "true") Boolean linkJob,
            @Parameter(description
                    = "The Job ID of the refresh job entry to 're-run', i.e. where to source User ID values from.  "
                    + "Will be auto set to the new Job ID if `linkJob=true`.")
            @RequestParam(required = false, defaultValue = "0") Long linkedJobId,
            @Parameter(description
                = "The status of the Job. This will default to `NEW`.  "
                + "Other recognised status values include: `ABORTED` and `COMPLETED`.")
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String comments,
            @Nullable
            @RequestBody(required = false) UserRequest userRequest) {
        if (userRequest == null) {
            userRequest = new UserRequest(); // default it to empty object
        }
        RefreshJobEntity newJob = RefreshJobEntity.builder()
                .jurisdiction(jurisdiction)
                .roleCategory(roleCategory)
                .status(status != null ? status : "NEW")
                .userIds(CollectionUtils.isNotEmpty(userRequest.getUserIds())
                        ? userRequest.getUserIds().toArray(new String[0]) : null)
                .linkedJobId(linkedJobId)
                .comments(comments)
                .created(ZonedDateTime.now())
                .build();
        newJob = persistenceService.persistRefreshJob(newJob);
        if (Boolean.TRUE.equals(linkJob)) {
            newJob.setLinkedJobId(newJob.getJobId());
            newJob = persistenceService.persistRefreshJob(newJob);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(convertRefreshJob(newJob));
    }

    @Operation(summary = "Get a Refresh Job")
    @GetMapping(value = "/am/testing-support/jobs/{jobId}")
    @ResponseStatus(code = HttpStatus.OK)
    @ApiResponse(
            responseCode = "200",
            description = "Successful",
            content = { @Content(schema = @Schema(implementation = RefreshJob.class), mediaType = "application/json") }
    )
    @ApiResponse(
            responseCode = "404",
            description = "Refresh Job not found",
            content = @Content()
    )
    public ResponseEntity<RefreshJob>  fetchJob(@Parameter(required = true) @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        return refreshJobEntity
                .map(jobEntity -> ResponseEntity.status(HttpStatus.OK).body(convertRefreshJob(jobEntity)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Operation(summary = "Delete a Refresh Job")
    @DeleteMapping(value = "/am/testing-support/jobs/{jobId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @ApiResponse(
            responseCode = "204",
            description = "No Content",
            content = @Content()
    )
    public ResponseEntity<Void>  removeJob(@Parameter(required = true) @PathVariable("jobId") Long jobId) {
        Optional<RefreshJobEntity> refreshJobEntity = persistenceService.fetchRefreshJobById(jobId);
        refreshJobEntity.ifPresent(jobEntity -> persistenceService.deleteRefreshJob(jobEntity));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private static RefreshJob convertRefreshJob(Object from) {
        return MAPPER.convertValue(from, new TypeReference<>() {});
    }

}
