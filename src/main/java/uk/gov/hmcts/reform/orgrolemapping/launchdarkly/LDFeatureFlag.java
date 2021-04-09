package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LDFeatureFlag {
    String flagName;
    boolean status;
}
