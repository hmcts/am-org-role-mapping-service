package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
@ExtendWith(MockitoExtension.class)
public class OrganisationServiceTest {
    private final String num_days = "90";

    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            Mockito.mock(OrganisationRefreshQueueRepository.class);

    OrganisationService organisationService = new OrganisationService(
            organisationRefreshQueueRepository, "90");

    @Test
    void DeleteActiveOrganisationRefreshRecordsTest() {
        organisationService.deleteActiveOrganisationRefreshRecords();

        verify(organisationRefreshQueueRepository, times(1))
                .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(num_days);
    }
}
