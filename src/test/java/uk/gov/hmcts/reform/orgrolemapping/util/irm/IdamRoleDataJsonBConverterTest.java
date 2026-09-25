package uk.gov.hmcts.reform.orgrolemapping.util.irm;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdamRoleDataJsonBConverterTest {

    private static final String JSON_FILENAME = "idamRoleData.json";
    private static final String ACTIVE_FLAG = "Y";
    private static final String DELETED_FLAG = "N";
    private static final String EMAIL_ID = "someone@somewhere.com";
    private static final String EXAMPLE_JSON = """
            {
              "email_id": "someone@somewhere.com",
              "active_flag": "Y",
              "deleted_flag": "N",
              "roles": [
                {
                  "role_name": "Role1"
                },
                {
                  "role_name": "Role2"
                }
              ]
            }
            """;
    private static final String ROLE1 = "Role1";
    private static final String ROLE2 = "Role2";

    @InjectMocks
    private IdamRoleDataJsonBConverter sut = new IdamRoleDataJsonBConverter();

    @Test
    void convertToDatabaseColumn() {
        IdamRoleData idamRoleData = TestDataBuilder.buildIdamRoleDataFromFile(JSON_FILENAME);
        String result = sut.convertToDatabaseColumn(idamRoleData);
        assertEquals(removeWhitespace(EXAMPLE_JSON), removeWhitespace(result));
    }

    @Test
    void convertToDatabaseColumn_Null() {
        String result = sut.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute() {
        IdamRoleData result = sut.convertToEntityAttribute(EXAMPLE_JSON);
        assertNotNull(result);
        assertEquals(EMAIL_ID, result.getEmailId());
        assertEquals(ACTIVE_FLAG, result.getActiveFlag());
        assertEquals(DELETED_FLAG, result.getDeletedFlag());
        assertEquals(2, result.getRoles().size());
        assertEquals(ROLE1, result.getRoles().get(0).getRoleName());
        assertEquals(ROLE2, result.getRoles().get(1).getRoleName());
    }

    @Test
    void convertToNullEntityAttribute() {
        IdamRoleData result = sut.convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void convertWrongJsonToEntityAttribute() {
        String wrongJson = EXAMPLE_JSON.replace(",","");
        assertThatThrownBy(() -> sut.convertToEntityAttribute(wrongJson))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unable to deserialize from json");
    }

    private String removeWhitespace(String input) {
        return input.replaceAll("\\s+", "");
    }
}
