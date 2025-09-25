package uk.gov.hmcts.reform.orgrolemapping.domain.model.enums;

import java.util.List;

public enum Jurisdiction {

    CIVIL("CIVIL", List.of("AAA6", "AAA7")),
    EMPLOYMENT("EMPLOYMENT", List.of("BHA1"));

    private final String name;
    private final List<String> serviceCodes;

    Jurisdiction(String name, List<String> serviceCodes) {
        this.name = name;
        this.serviceCodes = serviceCodes;
    }

    public String getName() {
        return name;
    }

    public List<String> getServiceCodes() {
        return serviceCodes;
    }

}
