package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;


class OrmDeserializerTest {

    private ObjectMapper mapper = new ObjectMapper();

    OrmDeserializer sut;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sut = new OrmDeserializer(mapper);
    }

    @Test
    void deserialize() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            sut.deserialize("4dc7dd3c-3fb5-4611-bbde-5101a97681e0");
        });
    }
}