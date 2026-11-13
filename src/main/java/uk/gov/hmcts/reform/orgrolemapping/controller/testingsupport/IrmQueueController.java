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

import java.util.ArrayList;
import java.util.List;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class IrmQueueController {

    public static final String FIND_QUEUE_ENTITY
            = "/am/testing-support/irm/findQueueEntity";
    public static final String MAKE_IRM_QUEUE_INACTIVE
            = "/am/testing-support/irm/makeIrmQueueInactive";

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
                .map(ResponseEntity::ok
                )
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(
        path = MAKE_IRM_QUEUE_INACTIVE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "IRM Queue makeIrmQueueInactive",
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
    public ResponseEntity<IdamRoleManagementQueueEntity> makeIrmQueueInactive(
            @RequestParam() String userId
    ) {
        getIrmQueueEntities(userId).forEach(
                idamRoleManagementQueueEntity -> setIrmInactive(idamRoleManagementQueueEntity));
        return ResponseEntity.ok().build();
    }

    private List<IdamRoleManagementQueueEntity> getIrmQueueEntities(String userId) {
        List<IdamRoleManagementQueueEntity> irmQueueEntities = new ArrayList<>();
        if ("ALL".equalsIgnoreCase(userId)) {
            irmQueueEntities.addAll(idamRoleManagementQueueRepository.findAll());
        } else {
            var idamRoleManagementQueueEntityOptional = idamRoleManagementQueueRepository
                    .findById(userId);
            if (idamRoleManagementQueueEntityOptional.isPresent()) {
                irmQueueEntities.add(idamRoleManagementQueueEntityOptional.get());
            }
        }
        return irmQueueEntities;
    }

    private void setIrmInactive(IdamRoleManagementQueueEntity idamRoleManagementQueueEntity) {
        // if active then deactivate
        if (Boolean.TRUE.equals(idamRoleManagementQueueEntity.getActive())) {
            idamRoleManagementQueueEntity.setActive(false);
            idamRoleManagementQueueRepository.save(idamRoleManagementQueueEntity);
        }
    }
}