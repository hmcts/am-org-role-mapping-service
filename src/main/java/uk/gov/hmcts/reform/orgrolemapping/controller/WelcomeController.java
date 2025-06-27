package uk.gov.hmcts.reform.orgrolemapping.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import static org.springdoc.core.utils.Constants.SWAGGER_UI_URL;


@RestController
@Hidden
public class WelcomeController {


    @GetMapping(value = "/swagger")
    public RedirectView swaggerRedirect() {
        return new RedirectView(SWAGGER_UI_URL, true, false);
    }

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
