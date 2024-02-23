package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfessionalUserService {

    private final UserRefreshQueueRepository userRefreshQueueRepository;
    private final AccessTypesRepository accessTypesRepository;
    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper;

    private String activeUserRefreshDays;

    public ProfessionalUserService(UserRefreshQueueRepository userRefreshQueueRepository,
            AccessTypesRepository accessTypesRepository,
            ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper,
            @Value("${professional.role.mapping.scheduling.userRefreshCleanup.activeUserRefreshDays}")
                                   String activeUserRefreshDays) {
        this.userRefreshQueueRepository = userRefreshQueueRepository;
        this.accessTypesRepository = accessTypesRepository;
        this.professionalRefreshOrchestrationHelper = professionalRefreshOrchestrationHelper;
        this.activeUserRefreshDays = activeUserRefreshDays;
    }


    public void refreshUsers() {
        List<AccessTypesEntity> accessTypesEntities = accessTypesRepository.findAll();
        AccessTypesEntity accessTypesEntity = accessTypesEntities.get(0);
        refreshUsersForAccessType(accessTypesEntity);
    }

    private void refreshUsersForAccessType(AccessTypesEntity accessTypesEntity) {
        UserRefreshQueueEntity userRefreshQueueEntity = userRefreshQueueRepository.retrieveSingleActiveRecord();
        while (userRefreshQueueEntity != null) {
            userRefreshQueueEntity = refreshAndClearUserRecord(userRefreshQueueEntity, accessTypesEntity);
        }
    }

    @Transactional
    public UserRefreshQueueEntity refreshAndClearUserRecord(UserRefreshQueueEntity userRefreshQueueEntity,
                                                            AccessTypesEntity accessTypesEntity) {
        professionalRefreshOrchestrationHelper.refreshSingleUser(userRefreshQueueEntity, accessTypesEntity);
        userRefreshQueueRepository.clearUserRefreshRecord(userRefreshQueueEntity.getUserId(),
                LocalDateTime.now(), accessTypesEntity.getVersion());
        return userRefreshQueueRepository.retrieveSingleActiveRecord();
    }
}
