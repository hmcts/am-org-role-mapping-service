package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.io.IOException;

@Component
public class OrmCallbackDeserializer implements Deserializer<UserRequest> {

    private final ObjectMapper mapper;

    public OrmCallbackDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public UserRequest deserialize(String source) {
        try {
            return mapper.readValue(
                    source,
                    new TypeReference<UserRequest>() {
                    }
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
