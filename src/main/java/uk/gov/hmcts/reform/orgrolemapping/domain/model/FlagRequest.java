package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FlagRequest {

    private String flagName;
    private String env;
    private String serviceName;
    private Boolean status;
}

