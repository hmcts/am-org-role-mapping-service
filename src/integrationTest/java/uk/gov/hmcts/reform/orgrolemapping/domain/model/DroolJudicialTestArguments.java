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
    private String jurisdiction;
    private String testGroup;
    private String testName;
    private String description;
    private String outputLocation;
    private String jrdResponseFileName;
    private String rasRequestFileNameWithoutBooking;
    private String rasRequestFileNameWithBooking;

    // flag to use additional test scenarios for Additional Roles, i.e. tests adjusting Additional Role End Dates
    private boolean additionalRoleTest;
    // if supplied will use this fallback template when running expired Additional Role End Date scenario
    private String additionalRoleExpiredFallbackFileName;

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
        // clone properties but return the builder to allow overrides
        return DroolJudicialTestArguments.builder()
            .jurisdiction(this.jurisdiction)
            .testGroup(this.testGroup)
            .testName(this.testName)
            .description(this.description)
            .outputLocation(this.outputLocation)
            .jrdResponseFileName(this.jrdResponseFileName)
            .rasRequestFileNameWithoutBooking(this.rasRequestFileNameWithoutBooking)
            .rasRequestFileNameWithBooking(this.rasRequestFileNameWithBooking)
            .additionalRoleTest(this.additionalRoleTest)
            .additionalRoleExpiredFallbackFileName(this.additionalRoleExpiredFallbackFileName)
            .overrideMapValues(cloneAndOverrideMap(this.overrideMapValues, null))
            .turnOffFlags(this.turnOffFlags);
    }

}
