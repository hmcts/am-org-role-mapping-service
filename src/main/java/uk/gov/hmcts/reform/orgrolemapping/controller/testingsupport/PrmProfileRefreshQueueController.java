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
import uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport.domain.ProfileRefreshQueueValue;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.ProfileRefreshQueueRepository;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@ConditionalOnProperty(name = "testing.support.enabled", havingValue = "true")
public class PrmProfileRefreshQueueController {

    protected static final String FIND_PROFILE_REFRESH_QUEUE
        = "/am/testing-support/prm/findProfileRefreshQueue";
    protected static final String MAKE_PROFILE_REFRESH_QUEUE_ACTIVE
        = "/am/testing-support/prm/makeProfileRefreshQueueActive";

    private final ProfileRefreshQueueRepository profileRefreshQueueRepository;

    @Autowired
    public PrmProfileRefreshQueueController(ProfileRefreshQueueRepository profileRefreshQueueRepository) {
        this.profileRefreshQueueRepository = profileRefreshQueueRepository;
    }


    @GetMapping(
        path = FIND_PROFILE_REFRESH_QUEUE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM Profile Refresh Queue findProfileRefreshQueue",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = ProfileRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Profile not found",
        content = @Content()
    )
    public ResponseEntity<ProfileRefreshQueueValue> findProfileRefreshQueue(
        @RequestParam() String organisationProfileId
    ) {
        return profileRefreshQueueRepository.findById(organisationProfileId)
            .map(profileRefreshQueueEntity ->
                ResponseEntity.ok(convertToProfileRefreshQueueValue(profileRefreshQueueEntity))
            )
            .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping(
        path = MAKE_PROFILE_REFRESH_QUEUE_ACTIVE
    )
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
        summary = "PRM Profile Refresh Queue makeProfileRefreshQueueActive",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        })
    @ApiResponse(
        responseCode = "200",
        description = "OK",
        content = @Content(
            schema = @Schema(implementation = ProfileRefreshQueueValue.class),
            mediaType = APPLICATION_JSON_VALUE
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Profile not found",
        content = @Content()
    )
    public ResponseEntity<ProfileRefreshQueueValue> makeProfileRefreshQueueActive(
        @RequestParam() String organisationProfileId
    ) {
        var profileRefreshQueueEntityOptional = profileRefreshQueueRepository.findById(organisationProfileId);
        if (profileRefreshQueueEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var profileRefreshQueueEntity = profileRefreshQueueEntityOptional.get();

        // if not active then activate
        if (Boolean.FALSE.equals(profileRefreshQueueEntity.getActive())) {
            profileRefreshQueueEntity.setActive(true);
            profileRefreshQueueRepository.save(profileRefreshQueueEntity);
        }

        return ResponseEntity.ok(convertToProfileRefreshQueueValue(profileRefreshQueueEntity));
    }


    private static ProfileRefreshQueueValue convertToProfileRefreshQueueValue(ProfileRefreshQueueEntity entity) {
        return ProfileRefreshQueueValue.builder()
            .organisationProfileId(entity.getOrganisationProfileId())
            .accessTypesMinVersion(entity.getAccessTypesMinVersion())
            .active(entity.getActive())
            .build();
    }

}
