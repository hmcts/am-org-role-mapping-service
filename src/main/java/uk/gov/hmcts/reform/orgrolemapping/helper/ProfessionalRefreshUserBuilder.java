package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.launchdarkly.shaded.org.jetbrains.annotations.NotNull;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;

import java.io.InputStream;

@Setter
public class ProfessionalRefreshUserBuilder {

    public static ResponseEntity<GetRefreshUsersResponse> buildGetRefreshUsersResponse(String resource, String userId) {
        try (InputStream inputStream =
                 ProfessionalRefreshUserBuilder.class.getClassLoader()
                     .getResourceAsStream(resource)) {
            assert inputStream != null;
            ObjectMapper objectMapper = getObjectMapper();
            GetRefreshUsersResponse getRefreshUsersResponse = objectMapper.readValue(inputStream,
                    GetRefreshUsersResponse.class);
            getRefreshUsersResponse.getUsers().get(0).setUserIdentifier(userId);
            return ResponseEntity.ok(getRefreshUsersResponse);
        } catch (Exception e) {
            throw new BadRequestException("Either the request is not valid or sample json is missing.");
        }
    }

    @NotNull
    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
