package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.SOLICITOR_PROFILE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.buildProfessionalUserData;

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

        assertEquals(userRefreshEntity.getUserId(), "1");
        assertNotNull(userRefreshEntity.getLastUpdated());
        assertNotNull(userRefreshEntity.getDeleted());
        assertEquals(userRefreshEntity.getAccessTypes(), "{}");
        assertEquals(userRefreshEntity.getOrganisationId(), "org 1");
        assertEquals(userRefreshEntity.getOrganisationStatus(), "ACTIVE");
        assertTrue(Arrays.asList(userRefreshEntity.getOrganisationProfileIds()).contains(SOLICITOR_PROFILE));
    }
}
