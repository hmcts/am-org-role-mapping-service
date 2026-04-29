package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class IrmQueueController {

    protected static final String FIND_QUEUE_ENTITY
            = "/am/testing-support/irm/findQueueEntity";
    protected static final String MAKE_QUEUE_ENTITY_ACTIVE
            = "/am/testing-support/prm/makeQueueEntityActive";

    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Autowired
    public IrmQueueController(IdamRoleManagementQueueRepository idamRoleManagementQueueRepository) {
        this.idamRoleManagementQueueRepository = idamRoleManagementQueueRepository;
    }


    @GetMapping(
        path = FIND_QUEUE_ENTITY
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "IRM Queue findQueue",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = IdamRoleManagementQueueEntity.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content()
    )
    public ResponseEntity<IdamRoleManagementQueueEntity> findQueueEntity(
            @RequestParam() String userId
    ) {
        return idamRoleManagementQueueRepository.findById(userId)
                .map(entity ->
                        ResponseEntity.ok(entity)
                )
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(
            path = MAKE_QUEUE_ENTITY_ACTIVE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "IRM Queue makeQueueEntryActive",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = IdamRoleManagementQueueEntity.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content()
    )
    public ResponseEntity<IdamRoleManagementQueueEntity> makeQueueEntityActive(
            @RequestParam() String userId,
            @RequestParam(required = false) Boolean active
    ) {
        if ("ALL".equalsIgnoreCase(userId)) {
            setAllUsersActive(active);
        } else {
            if (!setSingleUserActive(userId, active)) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok().build();
    }

    private void setAllUsersActive(Boolean active) {
        idamRoleManagementQueueRepository.findAll().forEach(queueEntity -> {
            setActive(queueEntity, active);
        });
    }

    private boolean setSingleUserActive(String userId, Boolean active) {
        var queueEntityOptional = idamRoleManagementQueueRepository
                .findById(userId);
        if (queueEntityOptional.isPresent()) {
            setActive(queueEntityOptional.get(), active);
            return true;
        }
        return false;
    }

    private void setActive(IdamRoleManagementQueueEntity queueEntity, Boolean active) {
        // if active does not match the required value then update it.
        if (!active.equals(queueEntity.getActive())) {
            queueEntity.setActive(active);
            idamRoleManagementQueueRepository.save(queueEntity);
        }
    }
}
