package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import java.io.IOException;

@Slf4j
@Service
public class BulkAssignmentOrchestrator {
    //1. Call parse request service to extract userId List and validations.
    //2. Call retrieveDataService to fetch the single or multiple user profiles and validate the data.
    //   This might require a stub
    //3. Call request mapping service to apply the mapping rules and
    //   a) prepare role assignment requests
    //   b)Invoke RoleAssignmentService and audit the response.


    private static final Logger logger = LoggerFactory.getLogger(BulkAssignmentOrchestrator.class);

    @Value("${roleAssignmentAppUrl}")
    String roleAssignmentAmUrl;

    ClientHttpRequestFactory requestFactory = new
            HttpComponentsClientHttpRequestFactory(HttpClients.createDefault());

    RestTemplate restTemplate = new RestTemplate(requestFactory);

    public ResponseEntity<Object>  createOrgRoleMapping(UserRequest userRequest) throws IOException {

        String url = roleAssignmentAmUrl + "/am/role-assignments";
        AssignmentRequest assignmentRequest = TestDataBuilder
                .buildAssignmentRequest(false);
        ResponseEntity<Object> response = null;
        logger.debug("URL.........................  {}", url);
        try {
            HttpEntity<AssignmentRequest> entity = new HttpEntity<>(assignmentRequest, getHttpHeaders());
            response = restTemplate
                    .exchange(url, HttpMethod.POST, entity, Object.class);
            logger.debug(response.toString());
        } catch (HttpClientErrorException exception) {
            logger.error("Error while connecting RAS service..{}", exception);
        }
        return response;
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        //To do : tokens should be setup
        String authorisation = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIs";
        String serviceAuthorisation = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE2MDI2ODAwNjJ9.eTrBOVMQI4L";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authorisation);
        headers.set("ServiceAuthorization", "Bearer " + serviceAuthorisation);
        return headers;
    }
}
