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
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.UserRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class PrmUserRefreshQueueController {

    protected static final String FIND_USER_REFRESH_QUEUE
        = "/am/testing-support/prm/findUserRefreshQueue";
    protected static final String MAKE_USER_REFRESH_QUEUE_ACTIVE
        = "/am/testing-support/prm/makeUserRefreshQueueActive";

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    public PrmUserRefreshQueueController(UserRefreshQueueRepository userRefreshQueueRepository) {
        this.userRefreshQueueRepository = userRefreshQueueRepository;
    }


    @GetMapping(
        path = FIND_USER_REFRESH_QUEUE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM User Refresh Queue findUserRefreshQueue",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = UserRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content()
    )
    public ResponseEntity<UserRefreshQueueValue> findUserRefreshQueue(
        @RequestParam() String userId
    ) {
        return userRefreshQueueRepository.findById(userId)
            .map(userRefreshQueueEntity ->
                ResponseEntity.ok(convertToUserRefreshQueueValue(userRefreshQueueEntity))
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(
        path = MAKE_USER_REFRESH_QUEUE_ACTIVE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM User Refresh Queue makeUserRefreshQueueActive",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = UserRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content()
    )
    public ResponseEntity<UserRefreshQueueValue> makeUserRefreshQueueActive(
        @RequestParam() String userId
    ) {
        var userRefreshQueueEntityOptional = userRefreshQueueRepository
            .findById(userId);
        if (userRefreshQueueEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var userRefreshQueueEntity = userRefreshQueueEntityOptional.get();

        // if not active then activate
        if (Boolean.FALSE.equals(userRefreshQueueEntity.getActive())) {
            userRefreshQueueEntity.setActive(true);
            userRefreshQueueRepository.save(userRefreshQueueEntity);
        }

        return ResponseEntity.ok(convertToUserRefreshQueueValue(userRefreshQueueEntity));
    }


    private static UserRefreshQueueValue convertToUserRefreshQueueValue(
        UserRefreshQueueEntity entity) {
        return UserRefreshQueueValue.builder()
            .userId(entity.getUserId())
            .userLastUpdated(entity.getUserLastUpdated())
            .lastUpdated(entity.getLastUpdated())
            .deleted(entity.getDeleted())
            .organisationId((entity.getOrganisationId()))
            .organisationStatus(entity.getOrganisationStatus())
            .organisationProfileIds(entity.getOrganisationProfileIds())
            .accessTypes(entity.getAccessTypes())
            .accessTypesMinVersion(entity.getAccessTypesMinVersion())
            .retry(entity.getRetry())
            .retryAfter(entity.getRetryAfter())
            .active(entity.getActive())
            .build();
    }

}
