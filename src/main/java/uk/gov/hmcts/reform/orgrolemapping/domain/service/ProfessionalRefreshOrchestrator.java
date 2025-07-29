package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUsersResponse;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import java.util.Objects;

@Service
@Slf4j
public class ProfessionalRefreshOrchestrator {

    public static final String NO_ACCESS_TYPES_FOUND = "No access types found in database";
    public static final String PRD_USER_NOT_FOUND = "User with ID %s not found in PRD";
    public static final String EXPECTED_SINGLE_PRD_USER = "Expected single user for ID %s, found %s";
    private final AccessTypesRepository accessTypesRepository;
    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final PrdService prdService;
    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;
    private final ProcessEventTracker processEventTracker;

    public ProfessionalRefreshOrchestrator(AccessTypesRepository accessTypesRepository,
                                           UserRefreshQueueRepository userRefreshQueueRepository,
                                           PrdService prdService,
                                           ProfessionalRefreshOrchestrationHelper
                                                   professionalRefreshOrchestrationHelper,
                                           ProcessEventTracker processEventTracker) {
        this.accessTypesRepository = accessTypesRepository;
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.prdService = prdService;
        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;
        this.processEventTracker = processEventTracker;
    }

    @Transactional
    public ProcessMonitorDto refreshProfessionalUser(String userId) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(
                "PRM Process 6 - Refresh User - Single User Mode");
        processEventTracker.trackEventStarted(processMonitorDto);
        log.info("Single User refreshProfessionalUser for {}", userId);
        GetRefreshUsersResponse getRefreshUsersResponse;
        try {
            getRefreshUsersResponse = Objects.requireNonNull(prdService.getRefreshUser(userId).getBody());
        } catch (FeignException.NotFound feignClientException) {
            throw new ResourceNotFoundException(String.format(PRD_USER_NOT_FOUND, userId));
        }

        if (getRefreshUsersResponse.getUsers().size() > 1) {
            String message = String.format(EXPECTED_SINGLE_PRD_USER, userId,
                getRefreshUsersResponse.getUsers().size());
            //throw new ServiceException(message);
            processMonitorDto.addProcessStep(message);
            processMonitorDto.markAsSuccess();
            processEventTracker.trackEventCompleted(processMonitorDto);
            return processMonitorDto;
        }

        professionalRefreshOrchestrationHelper.upsertUserRefreshQueue(getRefreshUsersResponse.getUsers().get(0));

        professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueRepository.findByUserId(userId),
                getLatestAccessTypes());

        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
        return processMonitorDto;
    }

    @Transactional
    public void refreshProfessionalUsers() {
        professionalRefreshOrchestrationHelper.processActiveUserRefreshQueue(getLatestAccessTypes());
    }

    private AccessTypesEntity getLatestAccessTypes() {
        return accessTypesRepository.findFirstByOrderByVersionDesc().orElseThrow(
            () -> new ServiceException(NO_ACCESS_TYPES_FOUND)
        );
    }

}
