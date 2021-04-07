package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;

import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FlagConfigs {
    @Getter
    private static FlagConfigs flagConfigs = buildFlagConfig();

    private final Map<String,FlagConfig> flagConfigByFlagName = new HashMap<>();

    private FlagConfigs(Collection<FlagConfig> flags) {
        flags.forEach(f -> flagConfigByFlagName.put(f.getName(), f));
    }

    public FlagConfig get(String flagName) {
        return flagConfigByFlagName.get(flagName);
    }

    public Collection<FlagConfig> getValues() {
        return flagConfigByFlagName.values();
    }

    private static FlagConfigs buildFlagConfig() {
        InputStream input = JacksonUtils.class.getClassLoader().getResourceAsStream("flag_config.json");
        CollectionType listType = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(
                ArrayList.class,
                FlagConfig.class
        );
        List<FlagConfig> allFlags = new ArrayList<>();
        try {
            allFlags = JacksonUtils.MAPPER.readValue(input, listType);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new FlagConfigs(allFlags);
    }
}

