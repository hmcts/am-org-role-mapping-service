package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.OrganisationRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class PrmOrganisationRefreshQueueController {

    protected static final String FIND_ORGANISATION_REFRESH_QUEUE
        = "/am/testing-support/prm/findOrganisationRefreshQueue";
    protected static final String MAKE_ORGANISATION_REFRESH_QUEUE_ACTIVE
        = "/am/testing-support/prm/makeOrganisationRefreshQueueActive";

    private final OrganisationRefreshQueueRepository oranisationRefreshQueueRepository;

    @Autowired
    public PrmOrganisationRefreshQueueController(OrganisationRefreshQueueRepository oranisationRefreshQueueRepository) {
        this.oranisationRefreshQueueRepository = oranisationRefreshQueueRepository;
    }


    @GetMapping(
        path = FIND_ORGANISATION_REFRESH_QUEUE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM Organisation Refresh Queue findOrganisationRefreshQueue",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = OrganisationRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Organisation not found",
        content = @Content()
    )
    public ResponseEntity<OrganisationRefreshQueueValue> findOrganisationRefreshQueue(
        @RequestParam() String organisationId
    ) {
        return oranisationRefreshQueueRepository.findById(organisationId)
            .map(organisationRefreshQueueEntity ->
                ResponseEntity.ok(convertToOrganisationRefreshQueueValue(organisationRefreshQueueEntity))
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(
        path = MAKE_ORGANISATION_REFRESH_QUEUE_ACTIVE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM Organisation Refresh Queue makeOrganisationRefreshQueueActive",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = OrganisationRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Organisation not found",
        content = @Content()
    )
    public ResponseEntity<OrganisationRefreshQueueValue> makeOrganisationRefreshQueueActive(
        @RequestParam() String organisationOrganisationId
    ) {
        var organisationRefreshQueueEntityOptional = oranisationRefreshQueueRepository
            .findById(organisationOrganisationId);
        if (organisationRefreshQueueEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var organisationRefreshQueueEntity = organisationRefreshQueueEntityOptional.get();

        // if not active then activate
        if (Boolean.FALSE.equals(organisationRefreshQueueEntity.getActive())) {
            organisationRefreshQueueEntity.setActive(true);
            oranisationRefreshQueueRepository.save(organisationRefreshQueueEntity);
        }

        return ResponseEntity.ok(convertToOrganisationRefreshQueueValue(organisationRefreshQueueEntity));
    }


    private static OrganisationRefreshQueueValue convertToOrganisationRefreshQueueValue(
        OrganisationRefreshQueueEntity entity) {
        return OrganisationRefreshQueueValue.builder()
            .organisationId(entity.getOrganisationId())
            .organisationLastUpdated(entity.getOrganisationLastUpdated())
            .lastUpdated(entity.getLastUpdated())
            .accessTypesMinVersion(entity.getAccessTypesMinVersion())
            .retry(entity.getRetry())
            .retryAfter(entity.getRetryAfter())
            .active(entity.getActive())
            .build();
    }

}
