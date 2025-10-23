package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;

@Builder
@Getter
public class DroolJudicialTestArguments {
    private String description;
    private String outputLocation;
    private String jrdResponseFileName;
    private String rasRequestFileNameWithoutBooking;
    private String rasRequestFileNameWithBooking;
    private boolean additionalRoleTest;
    private Map<String, String> overrideMapValues;

    @Builder.Default
    private List<FeatureFlagEnum> turnOffFlags = List.of(); // default is all flags on

    public Arguments toArguments() {
        return Arguments.arguments(
            description,
            this
        );
    }

    public DroolJudicialTestArgumentsBuilder cloneBuilder() {
        return DroolJudicialTestArguments.builder()
            .description(this.description)
            .outputLocation(this.outputLocation)
            .jrdResponseFileName(this.jrdResponseFileName)
            .rasRequestFileNameWithoutBooking(this.rasRequestFileNameWithoutBooking)
            .rasRequestFileNameWithBooking(this.rasRequestFileNameWithBooking)
            .additionalRoleTest(this.additionalRoleTest)
            .overrideMapValues(cloneAndOverrideMap(this.overrideMapValues, null))
            .turnOffFlags(this.turnOffFlags);
    }

}
