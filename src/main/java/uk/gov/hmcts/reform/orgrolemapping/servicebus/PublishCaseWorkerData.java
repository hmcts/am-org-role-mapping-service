package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PublishCaseWorkerData {
    @JsonProperty
    private List<String> userIds;
}
