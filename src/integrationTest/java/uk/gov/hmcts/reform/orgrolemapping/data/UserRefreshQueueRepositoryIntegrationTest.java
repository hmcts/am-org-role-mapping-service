package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUserData;

@Transactional
public class UserRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldUpsertToUserRefreshQueue() {

        // GIVEN
        String id = "1";
        Integer accessTypeMinVersion = 1;
        List<ProfessionalUserData> professionalUserData = List.of(buildProfessionalUserData(id));

        // WHEN
        userRefreshQueueRepository.upsertToUserRefreshQueue(jdbcTemplate, professionalUserData, accessTypeMinVersion);

        // THEN
        assertSingleUserRefreshQueue(id, accessTypeMinVersion);
    }

    @Test
    public void shouldUpsertToUserRefreshQueueForLastUpdated() {

        // GIVEN
        String id = "123";
        Integer accessTypeMinVersion = 2;
        List<ProfessionalUserData> professionalUserData = List.of(buildProfessionalUserData(id));

        // WHEN
        userRefreshQueueRepository
            .upsertToUserRefreshQueueForLastUpdated(jdbcTemplate, professionalUserData, accessTypeMinVersion);

        // THEN
        assertSingleUserRefreshQueue(id, accessTypeMinVersion);

    }

    private void assertSingleUserRefreshQueue(String id, Integer expectedAccessTypeMinVersion) {

        Optional<UserRefreshQueueEntity> userRefreshQueueEntities = userRefreshQueueRepository.findById(id);
        assertTrue(userRefreshQueueEntities.isPresent(), "UserRefreshQueueEntity should be present");
        UserRefreshQueueEntity userRefreshEntity = userRefreshQueueEntities.get();

        // the following comparison is based on `IntTestDataBuilder.buildProfessionalUserData(id)`
        assertEquals(id, userRefreshEntity.getUserId());
        assertNotNull(userRefreshEntity.getLastUpdated());
        assertNotNull(userRefreshEntity.getUserLastUpdated());
        assertNotNull(userRefreshEntity.getDeleted());
        assertEquals("{}", userRefreshEntity.getAccessTypes());
        assertEquals("org " + id, userRefreshEntity.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity.getRetry());
        assertNotNull(userRefreshEntity.getRetryAfter());

        // also verify
        assertEquals(expectedAccessTypeMinVersion, userRefreshEntity.getAccessTypesMinVersion());
    }

}
