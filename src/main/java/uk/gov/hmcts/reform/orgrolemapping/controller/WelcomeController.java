package uk.gov.hmcts.reform.orgrolemapping.controller;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.TopicPublisher;

@RestController
@Slf4j
@NoArgsConstructor
public class WelcomeController {

    TopicPublisher topicPublisher;

    @Autowired
    public WelcomeController(final TopicPublisher topicPublisher) {

        this.topicPublisher = topicPublisher;

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
        return "Welcome to Organisation Role Mapping Service";
    }


    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseEntity<String> send(@RequestBody String body) {
        log.info("Sending message for event");
        topicPublisher.sendMessage(body);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
