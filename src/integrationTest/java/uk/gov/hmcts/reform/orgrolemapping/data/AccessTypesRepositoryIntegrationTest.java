package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccessTypesRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private AccessTypesRepository accessTypesRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    public void shouldGetAccessTypesEntity() {
        AccessTypesEntity accessTypes = accessTypesRepository.getAccessTypesEntity();

        assertNotNull(accessTypes);
        assertNotNull(accessTypes.getVersion());
        assertNotNull(accessTypes.getAccessTypes());
    }
}
