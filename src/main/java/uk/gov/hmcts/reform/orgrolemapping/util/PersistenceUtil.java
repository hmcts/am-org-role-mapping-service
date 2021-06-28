package uk.gov.hmcts.reform.orgrolemapping.util;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.FlagConfig;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.FlagRequest;

@Service
public class PersistenceUtil {

    public FlagConfig convertFlagRequestToFlagConfig(FlagRequest flagRequest) {
        return FlagConfig.builder()
                .flagName(flagRequest.getFlagName())
                .env(flagRequest.getEnv())
                .serviceName(flagRequest.getServiceName())
                .status(flagRequest.getStatus())
                .build();
    }
}
