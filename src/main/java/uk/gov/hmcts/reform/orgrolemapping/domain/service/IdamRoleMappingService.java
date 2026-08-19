package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class IdamRoleMappingService {

    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository;
    private IdamRoleDataJsonBConverter idamRoleDataJsonBConverter;
    private Boolean idamRoleManagementEnabled;

    @Autowired
    public IdamRoleMappingService(
            IdamRoleManagementQueueRepository idamRoleManagementQueueRepository,
            @Value("${idam.role.management.enabled}")
            String idamRoleManagementEnabled) {
        this.idamRoleManagementQueueRepository = idamRoleManagementQueueRepository;
        this.idamRoleDataJsonBConverter = new IdamRoleDataJsonBConverter();
        this.idamRoleManagementEnabled = Boolean.parseBoolean(idamRoleManagementEnabled);
    }

    @Transactional
    public void addToQueue(UserType userType, Map<String, IdamRoleData> idamRoleList) {
        if  (!idamRoleManagementEnabled) {
            return;
        }
        log.info("Adding users to idam role mapping queue, total users: {}", idamRoleList.size());
        idamRoleList.forEach((userId, idamRoleData) -> {
            idamRoleManagementQueueRepository.upsert(userId, userType.name(),
                    idamRoleDataJsonBConverter.convertToDatabaseColumn(idamRoleData),
                    LocalDateTime.now());
        });
    }
}
