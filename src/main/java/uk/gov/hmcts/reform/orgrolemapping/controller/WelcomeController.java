package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@NoArgsConstructor
@Hidden
public class WelcomeController {

    //This is just a test API
    @GetMapping(value = "/welcome")
    public String welcome() {
        return "Welcome to Organisation Role Mapping Service";
    }

    //This method needed for the functional tests, so that RAS gets enough time to create records.
    @PostMapping(value = "/sleep")
    public ResponseEntity<String> waitFor(String duration) {
        return ResponseEntity.ok("Sleep time for Functional tests is over");

    }
}
