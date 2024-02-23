package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping(value = "/am/testing-support/send2CrdTopic")
    public ResponseEntity<String> send2CRD(@RequestBody String body) {
        log.info("Sending message 2 CRD topic");
        crdTopicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @PostMapping(value = "/am/testing-support/send2JrdTopic")
    public ResponseEntity<String> send2JRD(@RequestBody String body) {
        log.info("Sending message 2 JRD topic");
        jrdTopicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

}
