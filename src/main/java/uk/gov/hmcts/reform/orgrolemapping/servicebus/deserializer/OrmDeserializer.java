package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.io.IOException;

@Component
public class OrmDeserializer implements Deserializer<UserRequest> {

    private final ObjectMapper mapper;

    public OrmDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public UserRequest deserialize(String userIds) {
        try {
            return mapper.readValue(
                    userIds,
                    new TypeReference<UserRequest>() {
                    }
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize the received message", e);
        }
    }
}
