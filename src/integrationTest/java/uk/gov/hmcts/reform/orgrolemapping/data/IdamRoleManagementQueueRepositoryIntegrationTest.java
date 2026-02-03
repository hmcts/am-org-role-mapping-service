package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class IdamRoleManagementQueueRepositoryIntegrationTest extends BaseTestIntegration {

    private static final String USER_TYPE = "JUDICIAL";
    private static final String DATA = """
            {
              "email_id": "someone@somewhere.com",
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

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    @Test
    public void shouldUpsertToIdamRoleManagementQueue() {

        // GIVEN
        String userId = "some-user-id";

        // WHEN
        idamRoleManagementQueueRepository.upsert(userId, USER_TYPE, DATA, LocalDateTime.now());

        // THEN
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                idamRoleManagementQueueRepository.findById(userId);
        assertIdamRoleManagementQueueEntity(idamRoleManagementQueueEntity,
                userId, USER_TYPE, DATA);
    }

    private void assertIdamRoleManagementQueueEntity(
            Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity,
            String userId, String userType, String data
    ) {
        assertTrue(idamRoleManagementQueueEntity.isPresent(),
                "IdamRoleManagementQueueEntity should be present");
        IdamRoleManagementQueueEntity result = idamRoleManagementQueueEntity.get();

        assertEquals(userId, result.getUserId(), "User ID should match");
        assertEquals(userType, result.getUserType(), "User Type should match");
        assertNotNull(result.getData(), "Data should not be null");
    }
}
