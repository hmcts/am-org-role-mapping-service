package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@Slf4j
@NoArgsConstructor
public class TopicController {

    JRDTopicPublisher jrdTopicPublisher;
    CRDTopicPublisher crdTopicPublisher;

    @Autowired
    public TopicController(final JRDTopicPublisher jrdTopicPublisher,
                             final CRDTopicPublisher crdTopicPublisher) {
        this.jrdTopicPublisher = jrdTopicPublisher;
        this.crdTopicPublisher = crdTopicPublisher;
    }


    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send2CRD")
    public ResponseEntity<String> send2CRD(@RequestBody String body) {
        log.info("Sending message 2 CRD topic");
        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        publishCaseWorkerData.setUserIds(Arrays.asList("4b141f3c-9d8b-4eb2-932e-23fa49336CRD"));
        crdTopicPublisher.sendMessage(publishCaseWorkerData);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send2JRD")
    public ResponseEntity<String> send2JRD(@RequestBody String body) {
        log.info("Sending message 2 JRD topic");
        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        publishCaseWorkerData.setUserIds(Arrays.asList("4b141f3c-9d8b-4eb2-932e-23fa49336JRD"));
        jrdTopicPublisher.sendMessage(publishCaseWorkerData);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}
