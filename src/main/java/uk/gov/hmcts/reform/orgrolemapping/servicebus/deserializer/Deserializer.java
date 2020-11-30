package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

import java.util.List;

public interface Deserializer<T> {

    T deserialize(List<byte[]> body);
}
