package uk.gov.hmcts.reform.orgrolemapping.feignclients.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.CRDFeignClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CRDFeignClientFallback implements CRDFeignClient {

    public static final String CRD_API_NOT_AVAILABLE = "The CRD API Service is not available";

    @Override
    public String getServiceStatus() {
        return CRD_API_NOT_AVAILABLE;
    }

    @Override
    public ResponseEntity<List<UserProfile>> createRoleAssignment(UserRequest userRequest) {
        Set<UserProfile> userProfiles = new HashSet<>();


        userRequest.getUsers().forEach(userId -> {
            try (InputStream inputStream =
                         CRDFeignClientFallback.class.getClassLoader().getResourceAsStream("userProfileSample.json")) {
                assert inputStream != null;
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                UserProfile userProfile = objectMapper.readValue(inputStream, UserProfile.class);
                userProfile.setId(userId);
                userProfiles.add(userProfile);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        });
        return ResponseEntity.ok(new ArrayList<>(userProfiles));
    }

}