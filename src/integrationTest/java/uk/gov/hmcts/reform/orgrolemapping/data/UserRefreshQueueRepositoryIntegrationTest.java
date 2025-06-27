package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUserData;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.refreshUserAndOrganisationsList;

@Transactional
public class UserRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldUpsertToUserRefreshQueue() {
        List<ProfessionalUserData> professionalUserData = List.of(buildProfessionalUserData(1));

        userRefreshQueueRepository.upsertToUserRefreshQueue(jdbcTemplate, professionalUserData, 1);

        List<UserRefreshQueueEntity> userRefreshQueueEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userRefreshEntity = userRefreshQueueEntities.get(0);

        assertEquals("1", userRefreshEntity.getUserId());
        assertNotNull(userRefreshEntity.getLastUpdated());
        assertNotNull(userRefreshEntity.getUserLastUpdated());
        assertNotNull(userRefreshEntity.getDeleted());
        assertEquals("{}", userRefreshEntity.getAccessTypes());
        assertEquals("org 1", userRefreshEntity.getOrganisationId());
        assertEquals("ACTIVE", userRefreshEntity.getOrganisationStatus());
        assertTrue(Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
        assertEquals(0, userRefreshEntity.getRetry());
        assertNotNull(userRefreshEntity.getRetryAfter());
    }

    @Test
    public void shouldInsertIntoUserRefreshQueue() {
        List<RefreshUserAndOrganisation> refreshUserAndOrganisationsList =
                List.of(refreshUserAndOrganisationsList(123));

        userRefreshQueueRepository.insertIntoUserRefreshQueueForLastUpdated(jdbcTemplate,
                refreshUserAndOrganisationsList, 1);

        List<UserRefreshQueueEntity> usersEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userEntity = usersEntities.get(0);

        assertEquals("123", userEntity.getUserId());
        assertEquals(1, userEntity.getAccessTypesMinVersion());
        assertEquals(0, userEntity.getRetry());
        assertNotNull(userEntity.getRetryAfter());
    }

}
