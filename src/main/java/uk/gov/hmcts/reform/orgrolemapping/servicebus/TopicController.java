package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> send2CRD(@RequestBody PublishCaseWorkerData body) {
        log.info("Sending message 2 CRD topic");
        //crdTopicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    //This method is reserved for ASB topic testing. Need to be removed later.
    @PostMapping(value = "/send2JRD")
    public ResponseEntity<String> send2JRD(@RequestBody PublishCaseWorkerData body) {
        log.info("Sending message 2 JRD topic");
        //jrdTopicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}
