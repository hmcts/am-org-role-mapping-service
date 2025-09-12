package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessTypesRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private AccessTypesRepository accessTypesRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    public void shouldGetAccessTypesEntity() {
        AccessTypesEntity accessTypes = accessTypesRepository.getAccessTypesEntity();

        assertNotNull(accessTypes);
        assertEquals(0, accessTypes.getVersion());
        assertNotNull(accessTypes.getAccessTypes());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    public void shouldUpdateAccessTypesEntity() {
        String accessTypeJson = "{\"accessTypes\": [\"type1\", \"type2\"]}";
        AccessTypesEntity accessTypes = accessTypesRepository.updateAccessTypesEntity(accessTypeJson);

        assertNotNull(accessTypes);
        assertEquals(1, accessTypes.getVersion());
        assertNotNull(accessTypes.getAccessTypes());
        assertEquals(accessTypeJson, accessTypes.getAccessTypes());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_access_types.sql"})
    public void shouldNotAllowNewAccessTypesEntity() {
        AccessTypesEntity accessTypeEntity = AccessTypesEntity.builder()
            .accessTypes("{\"accessTypes\": [\"type1\", \"type2\"]}")
            .version(999L)
            .build();

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () ->
            accessTypesRepository.save(accessTypeEntity)
        );

        assertTrue(exception.getMessage().contains("unique constraint \"access_types_one_row_key\""));
    }

}
