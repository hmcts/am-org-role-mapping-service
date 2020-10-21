package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
public class CRDFeignClientFallback implements CRDFeignClient {

    public static final String CRD_API_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return CRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<List<UserProfile>> createRoleAssignment(UserRequest userRequest) {
        try (InputStream inputStream =
                     AssignmentRequestBuilder.class.getClassLoader().getResourceAsStream("userProfileSample.json")) {
            assert inputStream != null;
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            List<UserProfile> userProfiles = Arrays.asList(objectMapper.readValue(inputStream, UserProfile[].class));
            System.out.println(userProfiles);
            return ResponseEntity.ok(userProfiles);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}