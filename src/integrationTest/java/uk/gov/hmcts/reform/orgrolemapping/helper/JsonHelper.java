package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import uk.gov.hmcts.reform.orgrolemapping.controller.utils.WiremockFixtures;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndExpandReplaceMap;

public class JsonHelper {

    protected final ObjectMapper mapper = JacksonUtils.MAPPER;

    public String readJsonFromFile(String fileName, Map<String, String> replaceMap) {
        Map<String, String> replaceMapClone = cloneAndExpandReplaceMap(replaceMap);

        String json = readJsonFromFile(fileName);

        for (Map.Entry<String, String> entry: replaceMapClone.entrySet()) {
            if (entry.getValue() == null) {
                // i.e. replace `"[[NULL_VALUE]]"` with `null`: rather than `"null"`
                json = json.replace("\"" + entry.getKey() + "\"", "null");
            } else if (entry.getKey().endsWith("_BOOLEAN]]")) {
                // i.e. replace `"[[REPLACE_BOOLEAN]]"` with `true` or `false`: rather than `"true"` or `"false"`
                json = json.replace("\"" + entry.getKey() + "\"", entry.getValue());
            } else {
                json = json.replace(entry.getKey(), entry.getValue());
            }
        }

        return json;
    }

    @SneakyThrows
    public String readJsonFromFile(String fileName)  {
        Object json;

        if (!fileName.endsWith(".json")) {
            fileName = String.format("/%s.json", fileName);
        }

        try (InputStream is = WiremockFixtures.class.getResourceAsStream(fileName)) {
            json = mapper.readValue(is, Object.class);
        }

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    public String readJsonArrayFromFiles(List<String> fileNames)  {
        return "[" + fileNames.stream()
            .map(this::readJsonFromFile)
            .collect(Collectors.joining(",")) + "]";
    }

}
