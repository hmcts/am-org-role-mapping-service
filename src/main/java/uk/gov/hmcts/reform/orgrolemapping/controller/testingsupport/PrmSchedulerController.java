package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
import uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.scheduler.Scheduler;
import uk.gov.hmcts.reform.orgrolemapping.util.ValidationUtil;

@RestController
@Slf4j
@NoArgsConstructor
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class PrmSchedulerController {

    private Scheduler scheduler;
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;
    private OrganisationService organisationService;


    @Autowired
    public PrmSchedulerController(Scheduler scheduler,
        BatchLastRunTimestampRepository batchLastRunTimestampRepository,
        OrganisationService organisationService) {
        this.scheduler = scheduler;
        this.batchLastRunTimestampRepository = batchLastRunTimestampRepository;
        this.organisationService = organisationService;
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
            content = @Content(schema = @Schema(implementation = Object.class))
    )
    public ResponseEntity<Object> findAndUpdateCaseDefinitionChanges() {
        ProcessMonitorDto processMonitorDto = scheduler.findAndUpdateCaseDefinitionChanges();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

    @GetMapping(
        path = "/am/testing-support/prm/findOrganisationsWithStaleProfiles"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "PRM Process 2 findOrganisationsWithStaleProfiles",
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
    public ResponseEntity<Object> findOrganisationsWithStaleProfiles() {
        ProcessMonitorDto processMonitorDto = scheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

    @GetMapping(
        path = "/am/testing-support/prm/findOrganisationChanges"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "PRM Process 3 findOrganisationChanges",
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
    public ResponseEntity<Object> findOrganisationChanges(
        @Parameter(description = "Timestamp to fetch organisations with last updated date/time >= since, "
            + "expected format: " + Constants.SINCE_TIMESTAMP_FORMAT)
        @RequestParam(required = false) String since
    ) {
        log.info("findOrganisationChanges called with since: {}", since);
        // Fetch / validate the last run timestamp entity
        BatchLastRunTimestampEntity batchLastRunTimestampEntity = organisationService.getBatchLastRunTimestampEntity();

        if (since != null) {
            // Validate the datetime format
            ValidationUtil.validateDateTimeFormat(Constants.SINCE_TIMESTAMP_FORMAT, since);

            // Set the last run timestamp
            batchLastRunTimestampEntity.setLastOrganisationRunDatetime(LocalDateTime.parse(since,
                DateTimeFormatter.ofPattern(Constants.SINCE_TIMESTAMP_FORMAT)));

            // Update the last run timestamp entity
            batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
        }

        ProcessMonitorDto processMonitorDto = scheduler
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);
    }

    @GetMapping(
        path = "/am/testing-support/prm/findUsersWithStaleOrganisations"
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "PRM Process 4 findUsersWithStaleOrganisations",
            security =
            {
                @SecurityRequirement(name = AUTHORIZATION),
                @SecurityRequirement(name = SERVICE_AUTHORIZATION)
            })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Object.class)))
    )
    public ResponseEntity<Object> findUsersWithStaleOrganisations() {
        List<ProcessMonitorDto> processMonitorDtos = scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();
        return ResponseEntity.status(HttpStatus.OK).body(processMonitorDtos);
    }

}
