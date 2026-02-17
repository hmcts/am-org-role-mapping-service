package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdamRoleMappingServiceTest {

    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository
            = mock(IdamRoleManagementQueueRepository.class);

    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter =
            new IdamRoleDataJsonBConverter();

    private final IdamRoleMappingService sut =
            new IdamRoleMappingService(idamRoleManagementQueueRepository);

    private static final String[] EMAILS = {"email1@test.com", "email2@test.com"};
    private static final String[] ROLES = {"Role1", "Role2", "Role3"};
    private static final String[] USERS = {"user1", "user2"};

    @Test
    void addToQueueTest_Judicial() {
        addToQueueTest(UserType.JUDICIAL);
    }

    @Test
    void addToQueueTest_CaseWorker() {
        addToQueueTest(UserType.CASEWORKER);
    }

    @Captor
    private ArgumentCaptor<String> userIdCaptor;

    @Captor
    private ArgumentCaptor<String> dataCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> lastUpdatedCaptor;

    private void addToQueueTest(UserType userType) {
        // GIVEN
        Map<String, IdamRoleData> idamRoleList = new HashMap<>();
        idamRoleList.put(USERS[0], buildIdamRoleData(EMAILS[0],
                List.of(buildIdamRoleDataRole(ROLES[0]), buildIdamRoleDataRole(ROLES[1]))));
        idamRoleList.put(USERS[1], buildIdamRoleData(EMAILS[1],
                List.of(buildIdamRoleDataRole(ROLES[2]))));
        LocalDateTime startTime = LocalDateTime.now();

        // WHEN
        sut.addToQueue(userType, idamRoleList);

        // THIS
        verify(idamRoleManagementQueueRepository, times(idamRoleList.size()))
                .upsert(userIdCaptor.capture(), any(), any(),
                        dataCaptor.capture(), lastUpdatedCaptor.capture());

        assertLastUpdated(startTime, idamRoleList.size());

        assertNotNull(userIdCaptor.getAllValues());
        assertEquals(USERS.length, userIdCaptor.getAllValues().size());
        userIdCaptor.getAllValues().forEach(userId ->
                assertTrue(List.of(USERS).contains(userId)));

        assertNotNull(dataCaptor.getAllValues());
        assertEquals(USERS.length, dataCaptor.getAllValues().size());
        dataCaptor.getAllValues().forEach(data ->
            assertIdamRoleData(idamRoleDataJsonBConverter.convertToEntityAttribute(data)));

    }

    private void assertLastUpdated(LocalDateTime startTime, Integer noRowsExpected){
        assertNotNull(lastUpdatedCaptor.getAllValues());
        assertEquals(noRowsExpected, lastUpdatedCaptor.getAllValues().size());
        lastUpdatedCaptor.getAllValues().forEach(dateTime ->
                assertTrue(startTime.isBefore(dateTime)));
    }

    private void assertIdamRoleData(IdamRoleData idamRoleData) {
        assertNotNull(idamRoleData);
        assertTrue(Arrays.stream(EMAILS).toList().contains(idamRoleData.getEmailId()));
        assertEquals("Y",idamRoleData.getActiveFlag());
        assertEquals("N",idamRoleData.getDeletedFlag());
        idamRoleData.getRoles().forEach(idamRole ->
                assertTrue(Arrays.stream(ROLES).toList().contains(idamRole.getRoleName()))
        );
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
