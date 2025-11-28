package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class TestScenario {
    private String jurisdiction;
    private String testGroup;
    private String testName;
    private String description;
    private String outputLocation;
    private Map<String, String> replaceMap;

    // if test scenario needs to use an alternative Ras Request file template,
    //    e.g. Additional Role Fallback needed when role is expired but default role-assignments still generated
    private String overrideRasRequestFileName;
}
