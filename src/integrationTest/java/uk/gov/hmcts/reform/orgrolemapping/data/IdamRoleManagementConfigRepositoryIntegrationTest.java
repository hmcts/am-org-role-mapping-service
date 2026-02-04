package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementConfigEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementConfigRepository;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
public class IdamRoleManagementConfigRepositoryIntegrationTest extends BaseTestIntegration {

    private static final String LEGAL_CASEWORKER = "Legal Caseworker";
    private static final String SENIOR_LEGAL_CASEWORKER = "Senior Legal Caseworker";

    @Autowired
    private IdamRoleManagementConfigRepository idamRoleManagementConfigRepository;

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/irm/insert_flag_config.sql",
        "classpath:sql/irm/insert_idam_role_management_config.sql"})
    public void shouldFindAllForDeletion() {
        // local - All roles exist and set to true
        findAllForDeletionTest("local", List.of(SENIOR_LEGAL_CASEWORKER, LEGAL_CASEWORKER));

        // pr - One role set to true, one set to false
        findAllForDeletionTest("pr", List.of(SENIOR_LEGAL_CASEWORKER));

        // aat - No roles (One role set to false, one role does not exist)
        findAllForDeletionTest("aat", emptyList());

        // prod - No roles (No roles exist)
        findAllForDeletionTest("prod", emptyList());
    }

    private void findAllForDeletionTest(String env, List<String> roleNames) {
        // WHEN
        List<IdamRoleManagementConfigEntity> idamRoleManagementQueueEntity =
                idamRoleManagementConfigRepository.findAllForDeletion(env);

        // THEN
        assertNotNull(idamRoleManagementQueueEntity);
        assertEquals(roleNames.size(), idamRoleManagementQueueEntity.size());
        idamRoleManagementQueueEntity.forEach(entity ->
                assertEquals(true, roleNames.contains(entity.getRoleName())));
    }
}
