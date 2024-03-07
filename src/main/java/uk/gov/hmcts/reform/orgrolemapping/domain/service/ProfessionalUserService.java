package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import javax.transaction.Transactional;

@Service
public class ProfessionalUserService {

    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final ProcessEventTracker processEventTracker;

    private final String activeUserRefreshDays;

    public ProfessionalUserService(UserRefreshQueueRepository userRefreshQueueRepository,
            ProcessEventTracker processEventTracker,
            @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
            String activeUserRefreshDays) {
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.processEventTracker = processEventTracker;
        this.activeUserRefreshDays = activeUserRefreshDays;
    }

    @Transactional
    public void deleteActiveUserRefreshRecords() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("PRM Cleanup Process - User");
        processEventTracker.trackEventStarted(processMonitorDto);

        try {
            userRefreshQueueRepository
                    .deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(activeUserRefreshDays);

        } catch (Exception exception) {
            processMonitorDto.markAsFailed(exception.getMessage());
            processEventTracker.trackEventCompleted(processMonitorDto);
            throw exception;
        }

        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
    }
}
