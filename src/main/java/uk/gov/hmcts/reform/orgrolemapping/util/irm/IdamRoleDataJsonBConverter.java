package uk.gov.hmcts.reform.orgrolemapping.util.irm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;

@Slf4j
@Converter(autoApply = true)
public class IdamRoleDataJsonBConverter implements AttributeConverter<IdamRoleData, String> {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public String convertToDatabaseColumn(final IdamRoleData objectValue) {
        if (objectValue == null) {
            return null;
        }
        try {
            return mapper.writeValueAsString(objectValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize to json", e);
        }
    }

    @Override
    public IdamRoleData convertToEntityAttribute(final String dataValue) {

        if (dataValue == null) {
            return null;
        }
        try {
            return mapper.readValue(dataValue, IdamRoleData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize from json", e);
        }
    }
}
