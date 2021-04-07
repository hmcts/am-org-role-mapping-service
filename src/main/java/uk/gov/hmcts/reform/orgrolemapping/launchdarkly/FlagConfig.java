package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FlagConfig {
    private String name;
    @JsonProperty("default")
    private Boolean defaultValue;
    private List<Refresh> refresh;

    @Data
    public static class Refresh {
        private String jurisdiction;
        private String roleCategory;
    }
}
