package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class OrmDeserializer implements Deserializer<UserRequest> {

    private final ObjectMapper mapper;

    public OrmDeserializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public UserRequest deserialize(List<byte[]> messageBody) {
        try {
            var message = mapper.writeValueAsString(mapper.readValue(messageBody.get(0), Object.class));
            return mapper.readValue(message, UserRequest.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize the received message", e);
        }
    }
}
