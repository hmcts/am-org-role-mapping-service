package uk.gov.hmcts.reform.orgrolemapping.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.BaseTestIntegration;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUserAndOrganisation;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.orgrolemapping.helper.IntTestDataBuilder.refreshUserAndOrganisationsList;

@Transactional
public class UserRefreshQueueRepositoryIntegrationTest extends BaseTestIntegration {

    @Autowired
    private UserRefreshQueueRepository userRefreshQueueRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    public void shouldInsertIntoUserRefreshQueue() {
        List<RefreshUserAndOrganisation> refreshUserAndOrganisationsList = List.of(refreshUserAndOrganisationsList(123));

        userRefreshQueueRepository.insertIntoUserRefreshQueueForLastUpdated(jdbcTemplate, refreshUserAndOrganisationsList, 1);

        List<UserRefreshQueueEntity> usersEntities = userRefreshQueueRepository.findAll();
        UserRefreshQueueEntity userEntity = usersEntities.get(0);

        assertEquals( "123", userEntity.getUserId());
        assertEquals( 1, userEntity.getAccessTypesMinVersion());
    }
}