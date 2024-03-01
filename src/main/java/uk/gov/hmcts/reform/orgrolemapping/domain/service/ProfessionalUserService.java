package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

import javax.transaction.Transactional;

@Service
public class ProfessionalUserService {

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private final String activeUserRefreshDays;

    public ProfessionalUserService(UserRefreshQueueRepository userRefreshQueueRepository,
                                   @Value(
                                   "${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
            String activeUserRefreshDays) {
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.activeUserRefreshDays = activeUserRefreshDays;
    }

    @Transactional
    public void deleteActiveUserRefreshRecords() {
        userRefreshQueueRepository
                .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(activeUserRefreshDays);

    }
}
