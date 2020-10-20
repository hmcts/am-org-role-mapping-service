package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureToggleEvaluator;
import uk.gov.hmcts.reform.orgrolemapping.service.CreateOrgRoleMappingOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import java.io.IOException;

@RestController
@Slf4j
@NoArgsConstructor
public class WelcomeController {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    private CreateOrgRoleMappingOrchestrator createOrgRoleMappingOrchestrator;

    TopicPublisher topicPublisher;
    FeatureToggleEvaluator featureToggleEvaluator;


    @Autowired
    public WelcomeController(final TopicPublisher topicPublisher,
                             CreateOrgRoleMappingOrchestrator createOrgRoleMappingOrchestrator,
                             @Autowired FeatureToggleEvaluator featureToggleEvaluator) {

        this.topicPublisher = topicPublisher;
        this.createOrgRoleMappingOrchestrator = createOrgRoleMappingOrchestrator;
        this.featureToggleEvaluator = featureToggleEvaluator;

    }

    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    @GetMapping("/exception/{type}")
    public ResponseEntity<String> getException(@PathVariable String type) {
        if (type.equals("invalidRequest")) {
            throw new InvalidRequest("Invalid Request");
        } else if (type.equals("resourceNotFoundException")) {
            throw new ResourceNotFoundException("Resource Not Found Exception");
        } else if (type.equals("httpMessageConversionException")) {
            throw new HttpMessageConversionException("Http Message Conversion Exception");
        } else if (type.equals("badRequestException")) {
            throw new BadRequestException("Bad Request Exception");
        }

        return null;
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        //Use the below statement for any given API to implement Launch Darkly.

        featureToggleEvaluator.validateLdFlag("am-org-role-mapping-service", "orm-base-flag");
        return "Welcome to Organisation Role Mapping Service";
    }

    @PostMapping(
            path = "/am/role-mapping/staff/users",
            produces = V1.MediaType.CREATE_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    @ApiOperation("creates multiple role assignments")
    @ApiResponses({
            @ApiResponse(
                    code = 201,
                    message = "Created",
                    response = Object.class //need to replace with resource class
            ),
            @ApiResponse(
                    code = 400,
                    message = V1.Error.INVALID_ROLE_NAME
            ),
            @ApiResponse(
                    code = 400,
                    message = V1.Error.INVALID_REQUEST
            )
    })
    public ResponseEntity<Object> createOrgMapping(@RequestBody UserRequest userRequest)
            throws IOException {
        logger.debug("createOrgMapping");
        return createOrgRoleMappingOrchestrator.createOrgRoleMapping(userRequest);
    }


    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<String> send(@RequestBody String body) {
        log.info("Sending message for event");
        topicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
