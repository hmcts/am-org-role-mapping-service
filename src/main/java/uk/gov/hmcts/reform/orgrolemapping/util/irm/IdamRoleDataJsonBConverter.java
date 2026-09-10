package uk.gov.hmcts.reform.orgrolemapping.util.irm;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;

import static uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils.MAPPER;

@Slf4j
@Converter(autoApply = true)
public class IdamRoleDataJsonBConverter implements AttributeConverter<IdamRoleData, String> {

    @Override
    public String convertToDatabaseColumn(final IdamRoleData objectValue) {
        if (objectValue == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(objectValue);
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
            return MAPPER.readValue(dataValue, IdamRoleData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize from json", e);
        }
    }
}
