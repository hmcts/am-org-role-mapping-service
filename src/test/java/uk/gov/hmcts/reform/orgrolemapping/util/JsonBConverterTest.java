package uk.gov.hmcts.reform.orgrolemapping.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RunWith(MockitoJUnitRunner.class)
class JsonBConverterTest {

    @InjectMocks
    JsonBConverter sut = new JsonBConverter();

    @Test
    void convertToDatabaseColumn() {
        String result = sut.convertToDatabaseColumn(TestDataBuilder.buildAccessTypesFromFile());
        assertEquals(TestDataBuilder.buildAccessTypesFromFile().toString(), result);
    }

    @Test
    void convertToDatabaseColumn_Null() {
        String result = sut.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void convertToEntityAttribute() {
        String jsonString = """
                  {
                    "organisationProfiles": {
                    "SOLICITOR_ORG": {
                      "CIVIL": [
                        {
                          "accessTypeId": "BEFTA_SOLICITOR_1",
                          "accessMandatory": true,
                          "accessDefault": true,
                          "roles": [
                            {
                              "caseTypeId": "BEFTA_CASETYPE_1_1",
                              "organisationalRoleName": "OrgRoleName",
                              "groupRoleName": "GroupRoleName",
                              "caseGroupIdTemplate": "BEFTA_JUR:BEFTA_CaseType:[GrpRoleName1]:$ORGID$",
                              "groupAccessEnabled": true
                            }
                          ]
                        }
                      ]
                    }
                  }
                }""";

        JsonNode result = sut.convertToEntityAttribute(jsonString);

        assertEquals(TestDataBuilder.buildAccessTypesFromFile(), result);
    }

    @Test
    void convertToNullEntityAttribute() {
        JsonNode result = sut.convertToEntityAttribute(null);

        assertNull(result);
    }

    @Test
    void convertWrongJsonToEntityAttribute() {
        JsonNode result = sut.convertToEntityAttribute("{\"organisationProfiles\"}");

        assertNull(result);
    }
}
