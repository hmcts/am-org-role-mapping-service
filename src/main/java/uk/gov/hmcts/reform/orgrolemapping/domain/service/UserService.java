package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

import javax.transaction.Transactional;

@Service
public class UserService {

    private final UserRefreshQueueRepository userRefreshQueueRepository;

    private String activeUserRefreshDays;

    public UserService(UserRefreshQueueRepository userRefreshQueueRepository,
            @Value("${professional.role.mapping.scheduling.organisationRefreshCleanup.activeOrgansationRefreshDays}")
            String activeOrgansationRefreshDays) {
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.activeUserRefreshDays = activeUserRefreshDays;
    }


    @Transactional
    public void deleteActiveUserRefreshRecords() {
        userRefreshQueueRepository
                .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(activeUserRefreshDays);

    }
}
