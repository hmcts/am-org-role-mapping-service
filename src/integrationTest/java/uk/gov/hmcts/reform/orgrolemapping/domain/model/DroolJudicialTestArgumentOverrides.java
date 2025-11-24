package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class DroolJudicialTestArgumentOverrides {

    private String overrideDescription;

    // search criteria
    private String findJrdResponseFileName;
    private Map<String, String> findOverrideMapValues;

    // overrides
    private String overrideRasRequestFileNameWithoutBooking;
    private String overrideRasRequestFileNameWithBooking;

    @Builder.Default
    private List<FeatureFlagEnum> overrideTurnOffFlags = List.of(); // default is all flags on
}
