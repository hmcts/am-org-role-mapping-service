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
    private final String numDays = "90";

    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            Mockito.mock(OrganisationRefreshQueueRepository.class);

    private final OrganisationService organisationService = new OrganisationService(
            organisationRefreshQueueRepository, numDays);

    @Test
    void deleteActiveOrganisationRefreshRecordsTest() {
        organisationService.deleteActiveOrganisationRefreshRecords();

        verify(organisationRefreshQueueRepository, times(1))
                .deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
    }
}
