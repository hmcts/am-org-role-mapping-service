package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class TestScenario {
    private String description;
    private Map<String, String> replaceMap;
}
