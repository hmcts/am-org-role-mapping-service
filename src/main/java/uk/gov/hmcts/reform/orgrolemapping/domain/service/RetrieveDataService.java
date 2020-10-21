package uk.gov.hmcts.reform.orgrolemapping.domain.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.io.InputStream;

@Service
@Slf4j
public class RetrieveDataService {
    //1. Fetching multiple case-worker user details from CRD
        //a. Create a new class UserProfile - similar to expected response from CRD(refer LLD)
        //b. Create a new model class UserAccessProfile(id, roleId, roleName, primaryLocationId,
        // primaryLocationName, areaOfWorkId, serviceCode, deleteFlag) (which will flatten the User Profile into multiple
        // userAccessProfile instances based upon roleId X serviceCode).
    //2. Use CRDFeignClient to integrate with CRD and extend the fallback (to prepare some dummy userProfile and
        // userProfileAccess objects).
    //3. Call the parseRequestService to receive UserProfile and apply Validation wherever required.
    //4. Check for multiple Role and serviceCode, If yes prepare cartision product of R X S for UserAccessProfile
    //2. Fetching multiple judicial user details from JRD
    private final CRDFeignClient crdFeignClient;

    public RetrieveDataService(CRDFeignClient crdFeignClient) {
        this.crdFeignClient = crdFeignClient;
    }


    public void retrieveCaseWorkerProfiles(UserRequest userRequest) {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream("userProfileSample.json")) {
            assert inputStream != null;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            UserProfile userProfile = objectMapper.readValue(inputStream, UserProfile.class);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //JsonNode jsonNode = AssignmentRequestBuilder.buildAttributesFromFile("userProfileSample.json");

    }
}
