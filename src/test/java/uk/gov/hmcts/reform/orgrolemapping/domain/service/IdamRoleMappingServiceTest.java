package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdamRoleMappingServiceTest {

    private IdamRoleManagementQueueRepository idamRoleManagementQueueRepository
            = mock(IdamRoleManagementQueueRepository.class);

    private IdamRoleMappingService sut =
            new IdamRoleMappingService(idamRoleManagementQueueRepository);

    @Test
    void addToQueueTest_Judicial() {
        addToQueueTest(UserType.JUDICIAL);
    }

    @Test
    void addToQueueTest_CaseWorker() {
        addToQueueTest(UserType.CASEWORKER);
    }

    private void addToQueueTest(UserType userType) {
        // GIVEN
        Map<String, IdamRoleData> idamRoleList = new HashMap<>();
        idamRoleList.put("user1", buildIdamRoleData("email1@test.com",
                List.of(buildIdamRoleDataRole("role1"), buildIdamRoleDataRole("role2"))));
        idamRoleList.put("user2", buildIdamRoleData("email2@test.com",
                List.of(buildIdamRoleDataRole("role3"))));

        // WHEN
        sut.addToQueue(userType, idamRoleList);

        // THIS
        verify(idamRoleManagementQueueRepository, times(idamRoleList.size()))
                .upsert(any(), any(), any(), any(), any());
    }

    private IdamRoleData buildIdamRoleData(String email, List<IdamRoleDataRole> roles) {
        return IdamRoleData.builder()
                .emailId(email)
                .activeFlag("Y")
                .deletedFlag("N")
                .roles(roles)
                .build();
    }

    private IdamRoleDataRole buildIdamRoleDataRole(String role) {
        return IdamRoleDataRole.builder()
                .roleName(role)
                .build();
    }
}
