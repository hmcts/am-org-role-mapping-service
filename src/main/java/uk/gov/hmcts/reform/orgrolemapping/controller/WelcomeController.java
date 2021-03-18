package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.CRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.PublishCaseWorkerData;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.JRDTopicPublisher;
import uk.gov.hmcts.reform.orgrolemapping.v1.V1;

import java.util.Arrays;

@RestController
@Slf4j
@NoArgsConstructor
public class WelcomeController {

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    JRDTopicPublisher jrdTopicPublisher;
    CRDTopicPublisher crdTopicPublisher;

    @Autowired
    public WelcomeController(final JRDTopicPublisher jrdTopicPublisher,
                             final CRDTopicPublisher crdTopicPublisher,
                             BulkAssignmentOrchestrator bulkAssignmentOrchestrator) {

        this.jrdTopicPublisher = jrdTopicPublisher;
        this.crdTopicPublisher = crdTopicPublisher;
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;

    }

    @GetMapping(value = "/swagger")
    public String index() {
        return "redirect:swagger-ui.html";
    }

    @GetMapping(value = "/welcome")
    public String welcome() {
        //Use the below statement for any given API to implement Launch Darkly.
        return "Welcome to Organisation Role Mapping Service";
    }

    @PostMapping(
            path = "/am/role-mapping/staff/users",
            produces = V1.MediaType.MAP_ASSIGNMENTS,
            consumes = {"application/json"}
    )
    @ResponseStatus(code = HttpStatus.OK)
    @ApiOperation("creates multiple role assignments based upon user profile mapping rules")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "OK",
                    response = Object.class //need to replace with resource class
            ),
            @ApiResponse(
                    code = 400,
                    message = V1.Error.INVALID_REQUEST
            )
    })
    public ResponseEntity<Object> createOrgMapping(@RequestBody UserRequest userRequest) {
        long startTime = System.currentTimeMillis();
        log.debug("createOrgMapping");
        log.info("Process has been Started for the userIds {}",userRequest.getUserIds());
        ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(userRequest);
        log.info(
                "Execution time of createOrgMapping() : {} ms",
                (Math.subtractExact(System.currentTimeMillis(),startTime))
        );
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send2CRD")
    public ResponseEntity<String> send2CRD(@RequestBody String body) {
        log.info("Sending message 2 CRD topic");
        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        publishCaseWorkerData.setUserIds(Arrays.asList("4b141f3c-9d8b-4eb2-932e-23fa49336CRD"));
        crdTopicPublisher.sendMessage(publishCaseWorkerData);
        //topicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send2JRD")
    public ResponseEntity<String> send2JRD(@RequestBody String body) {
        log.info("Sending message 2 JRD topic");
        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        publishCaseWorkerData.setUserIds(Arrays.asList("4b141f3c-9d8b-4eb2-932e-23fa49336JRD"));
        jrdTopicPublisher.sendMessage(publishCaseWorkerData);
        //topicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    //This method needed for the functional tests, so that RAS gets enough time to create records.
    @PostMapping(value = "/sleep")
    public ResponseEntity<String> waitFor(String duration) {
        return ResponseEntity.ok("Sleep time for Functional tests is over");

    }
}
