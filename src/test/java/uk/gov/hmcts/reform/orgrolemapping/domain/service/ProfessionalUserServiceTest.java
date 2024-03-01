package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ProfessionalUserServiceTest {
    private final String numDays = "90";
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);

    ProfessionalUserService userService = new ProfessionalUserService(userRefreshQueueRepository, "90");

    @Test
    void deleteActiveUserRefreshRecordsTest() {
        userService.deleteActiveUserRefreshRecords();

        verify(userRefreshQueueRepository, times(1))
                .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(numDays);
    }
}
