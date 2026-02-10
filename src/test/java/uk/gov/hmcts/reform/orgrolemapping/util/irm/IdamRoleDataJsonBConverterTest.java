package uk.gov.hmcts.reform.orgrolemapping.util.irm;

import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.orgrolemapping.util.JsonBConverter;
import uk.gov.hmcts.reform.orgrolemapping.util.JsonBConverterTest;

class IdamRoleDataJsonBConverterTest extends JsonBConverterTest {

    private static final String JSON_FILENAME = "idamRoleData.json";
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

    @InjectMocks
    private JsonBConverter sut = new IdamRoleDataJsonBConverter();

    @Override
    protected JsonBConverter getSut() {
        return sut;
    }

    @Override
    protected String getJsonNodeFileName() {
        return JSON_FILENAME;
    }

    @Override
    protected String getExampleJson() {
        return EXAMPLE_JSON;
    }
}
