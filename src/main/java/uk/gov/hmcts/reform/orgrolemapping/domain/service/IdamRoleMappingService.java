package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class IdamRoleMappingService {

    private static final String JUDICIAL_QUEUE = "IRM Process Judicial Queue";

    @Autowired
    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;

    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter
            = new IdamRoleDataJsonBConverter();

    @Transactional
    public void addToQueue(UserType userType, Map<String, IdamRoleData> idamRoleList) {
        log.info("Adding users to idam role mapping queue, total users: {}", idamRoleList.size());
        idamRoleList.forEach((userId, idamRoleData) -> {
            idamRoleManagementQueueRepository.upsert(userId, userType.name(), "user",
                    idamRoleDataJsonBConverter.convertToDatabaseColumn(idamRoleData),
                    LocalDateTime.now());
        });
    }

    public ProcessMonitorDto processJudicialQueue() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto(JUDICIAL_QUEUE);
        processMonitorDto.markAsSuccess();
        return processMonitorDto;
    }
}
