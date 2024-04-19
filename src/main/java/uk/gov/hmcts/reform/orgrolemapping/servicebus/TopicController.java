package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.AUTHORIZATION;
import static uk.gov.hmcts.reform.orgrolemapping.apihelper.Constants.SERVICE_AUTHORIZATION;

@RestController
@Slf4j
@NoArgsConstructor
@ConditionalOnExpression("${testing.support.enabled} && (${amqp.crd.enabled} || ${amqp.jrd.enabled})")
public class TopicController {

    JRDTopicPublisher jrdTopicPublisher;
    CRDTopicPublisher crdTopicPublisher;

    @Autowired
    public TopicController(final JRDTopicPublisher jrdTopicPublisher,
                             final CRDTopicPublisher crdTopicPublisher) {
        this.jrdTopicPublisher = jrdTopicPublisher;
        this.crdTopicPublisher = crdTopicPublisher;
    }

    @PostMapping(
        path = "/am/testing-support/send2CrdTopic",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(
        summary = "sends message to the CRD topic in Azure Service Bus",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        }
    )
    @ApiResponse(
        responseCode = "204",
        description = "No Content",
        content = @Content()
    )
    public ResponseEntity<Void> send2CRD(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "message to send to the Azure Service Bus",
            required = true,
            content = @Content(schema = @Schema(implementation = UserRequest.class))
        )
        @RequestBody String body
    ) {
        log.info("Sending message 2 CRD topic");
        crdTopicPublisher.sendMessage(body);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
        path = "/am/testing-support/send2JrdTopic",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Operation(
        summary = "sends message to the JRD topic in Azure Service Bus",
        security = {
            @SecurityRequirement(name = AUTHORIZATION),
            @SecurityRequirement(name = SERVICE_AUTHORIZATION)
        }
    )
    @ApiResponse(
        responseCode = "204",
        description = "No Content",
        content = @Content()
    )
    public ResponseEntity<Void> send2JRD(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "message to send to the Azure Service Bus",
            required = true,
            content = @Content(schema = @Schema(implementation = UserRequest.class))
        )
        @RequestBody String body
    ) {
        log.info("Sending message 2 JRD topic");
        jrdTopicPublisher.sendMessage(body);
        return ResponseEntity.noContent().build();
    }

}
