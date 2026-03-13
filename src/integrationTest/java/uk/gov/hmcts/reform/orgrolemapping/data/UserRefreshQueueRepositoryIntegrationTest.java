package uk.gov.hmcts.reform.orgrolemapping.data;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.helper.ProfessionalUserBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUserData;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.refreshUser;

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

    @Test
    public void upsertToUserRefreshQueueForLastUpdated_whenNoOrganisationProfileIds() {

        // GIVEN
        int id = 2;
        Integer accessTypeMinVersion = 2;
        List<ProfessionalUserData> professionalUserData =
            List.of(buildProfessionalUserDataWithNoOrganisationProfileIds(id));

        // WHEN
        userRefreshQueueRepository
            .upsertToUserRefreshQueueForLastUpdated(jdbcTemplate, professionalUserData, accessTypeMinVersion);

        // THEN
        assertSingleUserRefreshQueue(String.valueOf(id), accessTypeMinVersion);
    }

    private ProfessionalUserData buildProfessionalUserDataWithNoOrganisationProfileIds(int id) {
        // Use exisiting helper function to convert from `refreshUser` object
        // so we format the empty list into a 'CSV' using the same techinque.
        RefreshUser refreshUser = refreshUser(id);
        refreshUser.getOrganisationInfo().setOrganisationProfileIds(null);
        ProfessionalUserData professionalUserData =
            ProfessionalUserBuilder.fromProfessionalRefreshUser(refreshUser);
        professionalUserData.setDeleted(LocalDateTime.now());
        professionalUserData.setAccessTypes("{}");
        professionalUserData.setOrganisationId("org 2");
        return professionalUserData;
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
        // Id 2 has no organisation profile ids
        if ("2".equals(id)) {
            assertEquals(0, Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).size());
        } else {
            assertTrue(
                Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        }
        assertEquals(0, userRefreshEntity.getRetry());
        assertNotNull(userRefreshEntity.getRetryAfter());

        // also verify
        assertEquals(expectedAccessTypeMinVersion, userRefreshEntity.getAccessTypesMinVersion());
    }

}
