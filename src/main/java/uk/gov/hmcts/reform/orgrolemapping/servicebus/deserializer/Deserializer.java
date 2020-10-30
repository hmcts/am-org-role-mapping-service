package uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer;

public interface Deserializer<T> {

    T deserialize(String source);
}
