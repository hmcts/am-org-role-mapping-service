package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.CASEWORKER;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;

@ExtendWith(MockitoExtension.class)
class IdamRoleMappingServiceTest {

    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository
            = mock(IdamRoleManagementQueueRepository.class);

    private final PlatformTransactionManager transactionManager
            = mock(PlatformTransactionManager.class);

    private final ProcessEventTracker processEventTracker
            = mock(ProcessEventTracker.class);

    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter =
            new IdamRoleDataJsonBConverter();

    private final IdamRoleMappingService sut =
            new IdamRoleMappingService(idamRoleManagementQueueRepository, transactionManager,
                    processEventTracker, "1", "2", "3");

    private static final String[] EMAILS = {"email1@test.com", "email2@test.com"};
    private static final String[] ROLES = {"Role1", "Role2", "Role3"};
    private static final String[] USERS = {"user1", "user2"};

    @Captor
    private ArgumentCaptor<String> userIdCaptor;

    @Captor
    private ArgumentCaptor<String> dataCaptor;

    @Captor
    private ArgumentCaptor<LocalDateTime> lastUpdatedCaptor;

    @ParameterizedTest
    @EnumSource(UserType.class)
    void addToQueueTest(UserType userType) {
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
                .upsert(userIdCaptor.capture(), any(),
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

    @ParameterizedTest
    @EnumSource(UserType.class)
    void processQueueTest_Success(UserType userType) {
        processQueueTest(userType, getIrmQueue(), EndStatus.SUCCESS);
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void processQueueTest_Partial(UserType userType) {
        processQueueTest(userType, getIrmQueue(), EndStatus.PARTIAL_SUCCESS);
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void processQueueTest_Failure(UserType userType) {
        processQueueTest(userType, getIrmQueue(), EndStatus.FAILED);
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void processQueueTest_NoRecords(UserType userType) {
        processQueueTest(userType, new ArrayList<>(), EndStatus.SUCCESS);
    }

    private void processQueueTest(UserType userType,
                                  List<IdamRoleManagementQueueEntity> irmQueue,
                                  EndStatus endStatus) {
        // GIVEN
        when(idamRoleManagementQueueRepository.findAndLockSingleActiveRecord(userType.name()))
                .thenReturn(irmQueue != null && !irmQueue.isEmpty() ? irmQueue.get(0) : null)
                .thenReturn(irmQueue != null && !irmQueue.isEmpty() ? irmQueue.get(1) : null)
                .thenReturn(null);
        when(transactionManager.getTransaction(any()))
                .thenReturn(mock(org.springframework.transaction.TransactionStatus.class));
        RuntimeException exception = new RuntimeException("Failed to process queue entry");
        if (EndStatus.PARTIAL_SUCCESS.equals(endStatus)) {
            when(idamRoleManagementQueueRepository.setAsPublished(any(), any()))
                    .thenReturn(1)
                    .thenThrow(exception);
        } else if (EndStatus.FAILED.equals(endStatus))  {
            when(idamRoleManagementQueueRepository.setAsPublished(any(), any()))
                    .thenThrow(exception);
        } else {
            when(idamRoleManagementQueueRepository.setAsPublished(any(), any()))
                    .thenReturn(1);
        }

        //WHEN
        ProcessMonitorDto processMonitorDto;
        if (JUDICIAL.equals(userType)) {
            processMonitorDto = sut.processJudicialQueue();
        } else {
            processMonitorDto = sut.processCaseWorkerQueue();
        }

        // THEN
        assertNotNull(processMonitorDto);
        assertEquals(endStatus, processMonitorDto.getEndStatus(), "Status is incorrect");
        assertEquals(String.format(IdamRoleMappingService.QUEUE_NAME, userType.name()),
                processMonitorDto.getProcessType(), "Process type is incorrect");
        // Verify the event is tracked as started
        verify(processEventTracker, times(1)).trackEventStarted(any());
        // Verify the queue is polled until no records are left
        verify(idamRoleManagementQueueRepository, times(irmQueue.size() + 1))
                .findAndLockSingleActiveRecord(userType.name());
        // Verify the records are marked as published
        verify(idamRoleManagementQueueRepository, times(irmQueue.size()))
                .setAsPublished(any(), any());
        // Verify the retries
        Integer retries = EndStatus.FAILED.equals(endStatus) ? irmQueue.size()
                : EndStatus.PARTIAL_SUCCESS.equals(endStatus) ? irmQueue.size() - 1
                : 0;
        verify(idamRoleManagementQueueRepository, times(retries))
                .updateRetry(any(), any(), any(), any());
        // Verify the event is tracked as ended
        verify(processEventTracker, times(1)).trackEventCompleted(processMonitorDto);
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void processQueueTest_Exception(UserType userType) {
        // GIVEN
        when(idamRoleManagementQueueRepository
                .findAndLockSingleActiveRecord(userType.name()))
                .thenThrow(new ServiceException("Exception thrown"));

        //WHEN
        if (CASEWORKER.equals(userType)) {
            Assertions.assertThrows(ServiceException.class, sut::processCaseWorkerQueue);
        } else {
            Assertions.assertThrows(ServiceException.class, sut::processJudicialQueue);
        }
    }

    private void assertLastUpdated(LocalDateTime startTime, Integer noRowsExpected) {
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

    private List<IdamRoleManagementQueueEntity> getIrmQueue() {
        String[] users = new String[] {"user1", "user2"};
        List<IdamRoleManagementQueueEntity> irmQueue = new ArrayList<>();
        Arrays.stream(users).forEach(user -> irmQueue.add(
                IdamRoleManagementQueueEntity.builder().userId(user).build()));
        return irmQueue;
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
