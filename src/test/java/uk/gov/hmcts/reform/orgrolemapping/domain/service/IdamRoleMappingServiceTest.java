package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.irm.IdamRoleManagementQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamInvitation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleData;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamRoleDataRole;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.irm.IdamUser;
import uk.gov.hmcts.reform.orgrolemapping.feignclients.IdamFeignClient;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;
import uk.gov.hmcts.reform.orgrolemapping.util.irm.IdamRoleDataJsonBConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.CASEWORKER;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType.JUDICIAL;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.IdamRoleMappingService.INVITEUSER_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.IdamRoleMappingService.UPDATEUSER_NAME;

@ExtendWith(MockitoExtension.class)
class IdamRoleMappingServiceTest {

    private final IdamFeignClient idamFeignClient = mock(IdamFeignClient.class);

    private final IdamRoleManagementQueueRepository idamRoleManagementQueueRepository
            = mock(IdamRoleManagementQueueRepository.class);

    private final PlatformTransactionManager transactionManager
            = mock(PlatformTransactionManager.class);

    private final ProcessEventTracker processEventTracker
            = mock(ProcessEventTracker.class);

    private final IdamRoleDataJsonBConverter idamRoleDataJsonBConverter =
            new IdamRoleDataJsonBConverter();

    private final IdamRoleMappingService sut =
            new IdamRoleMappingService(idamFeignClient, idamRoleManagementQueueRepository, transactionManager,
                    processEventTracker, "1", "2", "3");

    private static final String[] EMAILS = {"email1@test.com", "email2@test.com"};
    private static final String[] OLDROLES = {"OldRole1", "Role1"};
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
                new String[] {ROLES[0], ROLES[1]}));
        idamRoleList.put(USERS[1], buildIdamRoleData(EMAILS[1],
                new String[] {ROLES[2]}));
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
        ServiceException exception =
                new ServiceException("Error occurred while processing idam role mapping");
        if (EndStatus.PARTIAL_SUCCESS.equals(endStatus)) {
            when(idamFeignClient.getUserById(any()))
                    .thenReturn(ResponseEntity.ok(IdamUser.builder()
                            .id(irmQueue.getFirst().getUserId())
                            .roleNames(Arrays.stream(OLDROLES).toList())
                            .build()))
                    .thenThrow(exception);
            when(idamRoleManagementQueueRepository.findById(any()))
                    .thenReturn(Optional.of(irmQueue.get(0)));
            when(idamFeignClient.updateUser(any(), any()))
                    .thenReturn(ResponseEntity.ok(IdamUser.builder()
                            .roleNames(Arrays.stream(ROLES).toList())
                            .build()));
        } else if (EndStatus.FAILED.equals(endStatus))  {
            when(idamFeignClient.getUserById(any()))
                    .thenThrow(exception);
        } else {
            irmQueue.forEach(entity -> {
                when(idamFeignClient.getUserById(entity.getUserId()))
                        .thenReturn(ResponseEntity.ok(IdamUser.builder()
                                .id(entity.getUserId())
                                .roleNames(Arrays.stream(OLDROLES).toList())
                                .build()));
                when(idamRoleManagementQueueRepository.findById(entity.getUserId()))
                        .thenReturn(Optional.of(entity));
            });
            when(idamFeignClient.updateUser(any(), any()))
                    .thenReturn(ResponseEntity.ok(IdamUser.builder()
                            .roleNames(Arrays.stream(ROLES).toList())
                            .build()));
        }

        //WHEN
        ProcessMonitorDto processMonitorDto;
        if (JUDICIAL.equals(userType)) {
            processMonitorDto = sut.processJudicialQueue();
        } else {
            processMonitorDto = sut.processCaseWorkerQueue();
        }

        // THEN
        assertProcessMonitor(processMonitorDto, endStatus, userType, exception);
        // Verify the event is tracked as started
        verify(processEventTracker, times(irmQueue.isEmpty() ? 1 : 3)).trackEventStarted(any());
        // Verify the queue is polled until no records are left
        verify(idamRoleManagementQueueRepository, times(irmQueue.size() + 1))
                .findAndLockSingleActiveRecord(userType.name());
        // Verify the records are marked as published
        verify(idamRoleManagementQueueRepository,
                times(EndStatus.FAILED.equals(endStatus) ? 0 :
                        EndStatus.PARTIAL_SUCCESS.equals(endStatus) ? 1 : irmQueue.size()))
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

    @Test
    void getUserTest() {
        // GIVEN
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        ResponseEntity<IdamUser> expectedResult = ResponseEntity.ok(user);
        when(idamFeignClient.getUserById(any())).thenReturn(expectedResult);

        //WHEN
        IdamUser result = sut.getIdamUser(user.getId());

        // THEN
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        verify(idamFeignClient,times(1)).getUserById(user.getId());
    }

    @Test
    void getUserByEmailTest() {
        // GIVEN
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        ResponseEntity<IdamUser> expectedResult = ResponseEntity.ok(user);
        when(idamFeignClient.getUserByEmail(any())).thenReturn(expectedResult);

        //WHEN
        IdamUser result = sut.getIdamUserByEmail(user.getEmail());

        // THEN
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(idamFeignClient,times(1)).getUserByEmail(user.getEmail());
    }

    @Test
    void patchUserTest() {
        // GIVEN
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], new ArrayList<>());
        IdamRoleData idamRoleData = buildIdamRoleData(EMAILS[0], ROLES);
        ResponseEntity<IdamUser> expectedResult = ResponseEntity.ok(user);
        when(idamFeignClient.updateUser(any(), any())).thenReturn(expectedResult);

        //WHEN
        boolean result = sut.patchIdamUser(user, idamRoleData);

        // THEN
        assertTrue(result);
        verify(idamFeignClient,times(1)).updateUser(user.getId(), user);
    }

    @Test
    void updateUserTest_Success() {
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        IdamRoleData idamRoleData = buildIdamRoleData(EMAILS[0], ROLES);
        updateUserTest(user, idamRoleData, EndStatus.SUCCESS);
    }

    @Test
    void updateUserTest_Exception() {
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        IdamRoleData idamRoleData = buildIdamRoleData(EMAILS[0], ROLES);
        updateUserTest(user, idamRoleData, EndStatus.FAILED);
    }

    @Test
    void updateUserTest_Nonexistant() {
        updateUserTest(null, null, EndStatus.FAILED);
    }

    private void updateUserTest(IdamUser user, IdamRoleData idamRoleData, EndStatus endStatus) {
        // GIVEN
        String userId = user != null ? user.getId() : null;
        ResponseEntity<IdamUser> expectedResult = ResponseEntity.ok(user);
        Optional<IdamRoleManagementQueueEntity> idamRoleManagementQueueEntity =
                Optional.of(IdamRoleManagementQueueEntity.builder()
                .userId(userId)
                .data(idamRoleData)
                .build());
        if (EndStatus.SUCCESS.equals(endStatus)) {
            when(idamFeignClient.getUserById(userId)).thenReturn(expectedResult);
            when(idamRoleManagementQueueRepository.findById(userId)).thenReturn(idamRoleManagementQueueEntity);
            when(idamFeignClient.updateUser(any(), any())).thenReturn(expectedResult);
        } else if (EndStatus.FAILED.equals(endStatus)) {
            when(idamFeignClient.getUserById(userId)).thenReturn(expectedResult);
            when(idamRoleManagementQueueRepository.findById(userId)).thenReturn(idamRoleManagementQueueEntity);
            when(idamFeignClient.updateUser(any(), any()))
                    .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Error"));
        }

        // WHEN
        ProcessMonitorDto result = sut.updateUser(userId);

        // THEN
        assertNotNull(result);
        assertEquals(endStatus, result.getEndStatus());
        assertEquals(UPDATEUSER_NAME, result.getProcessType());
        verify(idamFeignClient,times(user != null ? 1 : 0)).updateUser(any(), any());
    }

    @Test
    void inviteUserTest_Success() {
        // GIVEN
        IdamUser newUser0 = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        IdamUser oldUser0 = buildIdamUser(USERS[0], EMAILS[0], Collections.emptyList());
        IdamUser oldUser1 = buildIdamUser(USERS[1], EMAILS[0], Arrays.stream(ROLES).toList());
        List<IdamInvitation> oldInvitations = List.of(
                sut.buildInvitationFromUser(oldUser0, Arrays.stream(OLDROLES).toList()),
                sut.buildInvitationFromUser(oldUser1, Arrays.stream(OLDROLES).toList()));

        // WHEN
        inviteUserTest(newUser0, oldInvitations, CREATED, EndStatus.SUCCESS);
    }

    @Test
    void inviteUserTest_Failure() {
        // GIVEN
        IdamUser newUser0 = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        IdamUser oldUser0 = buildIdamUser(USERS[0], EMAILS[0], Collections.emptyList());
        IdamUser oldUser1 = buildIdamUser(USERS[1], EMAILS[0], Arrays.stream(OLDROLES).toList());
        List<IdamInvitation> oldInvitations = List.of(
                sut.buildInvitationFromUser(oldUser0, Arrays.stream(OLDROLES).toList()),
                sut.buildInvitationFromUser(oldUser1, Arrays.stream(OLDROLES).toList()));

        // WHEN
        inviteUserTest(newUser0, oldInvitations, INTERNAL_SERVER_ERROR, EndStatus.FAILED);
    }

    @Test
    void inviteUserTest_Exception() {
        IdamUser user = buildIdamUser(USERS[0], EMAILS[0], Arrays.stream(ROLES).toList());
        inviteUserTest(user, Collections.emptyList(), BAD_REQUEST, EndStatus.FAILED);
    }

    @Test
    void inviteUserTest_NonexistantSuccess() {
        inviteUserTest(null, Collections.emptyList(), CREATED, EndStatus.SUCCESS);
    }

    private void inviteUserTest(IdamUser user, List<IdamInvitation> oldInvitations,
                                HttpStatus httpStatus, EndStatus endStatus) {
        // GIVEN
        String email = user != null ? user.getEmail() : EMAILS[0];
        ResponseEntity<IdamUser> expectedUserResult = ResponseEntity.ok(user);
        ResponseEntity<List<IdamInvitation>> expectedOldInvitationResults = ResponseEntity.ok(oldInvitations);
        when(idamFeignClient.getUserByEmail(email)).thenReturn(expectedUserResult);
        when(idamFeignClient.getInvitations(email)).thenReturn(expectedOldInvitationResults);
        // BAD_REQUEST emulates throwing an exception on invitation creation
        if (BAD_REQUEST.equals(httpStatus)) {
            when(idamFeignClient.inviteUser(any()))
                    .thenThrow(new HttpClientErrorException(BAD_REQUEST, "Error"));
        } else {
            IdamUser invitationUser = user != null ? user : sut.buildIdamUserFromEmail(email);
            ResponseEntity<IdamInvitation> expectedNewInvitationResult =
                    new ResponseEntity<>(sut.buildInvitationFromUser(invitationUser,
                            Arrays.stream(ROLES).toList()), httpStatus);
            when(idamFeignClient.inviteUser(any())).thenReturn(expectedNewInvitationResult);
        }

        // WHEN
        ProcessMonitorDto result = sut.inviteUser(email, Arrays.stream(ROLES).toList());

        // THEN
        assertNotNull(result);
        assertEquals(endStatus, result.getEndStatus());
        assertEquals(INVITEUSER_NAME, result.getProcessType());
        verify(idamFeignClient,times(1)).getUserByEmail(any());
        verify(idamFeignClient,times(1)).getInvitations(any());
        verify(idamFeignClient,times(oldInvitations.size())).deleteInvitation(any());
        verify(idamFeignClient,times(1)).inviteUser(any());
    }

    private void assertProcessMonitor(ProcessMonitorDto processMonitorDto, EndStatus expectedStatus,
                              UserType userType, Exception exception) {
        assertNotNull(processMonitorDto);
        // StartTime
        assertTrue(processMonitorDto.getStartTime().isAfter(LocalDateTime.now().minusMinutes(1)),
                "Start time should be recent");
        // EndTime
        assertTrue(processMonitorDto.getEndTime().isAfter(processMonitorDto.getStartTime()),
                "End time should be after start time");
        // EndStatus
        assertEquals(expectedStatus, processMonitorDto.getEndStatus(), "Status is incorrect");
        // ProcessSteps
        assertNotNull(processMonitorDto.getProcessSteps(), "Process Steps should be present");
        // ProcessType
        assertEquals(String.format(IdamRoleMappingService.QUEUE_NAME, userType.name()),
                processMonitorDto.getProcessType(), "Process type is incorrect");
        // EndDetail
        if (!EndStatus.SUCCESS.equals(expectedStatus)) {
            assertNotNull(processMonitorDto.getEndDetail(), "End Detail should be present");
            assertTrue(processMonitorDto.getEndDetail().contains(exception.getMessage()),
                    "End Detail should contain exception message");
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
        List<IdamRoleManagementQueueEntity> irmQueue = new ArrayList<>();
        for (int i = 0; i < USERS.length; i++) {
            irmQueue.add(IdamRoleManagementQueueEntity.builder()
                            .userId(USERS[i])
                            .data(buildIdamRoleData(EMAILS[0], ROLES))
                            .build());
        }

        return irmQueue;
    }

    private IdamRoleData buildIdamRoleData(String email, String[] roles) {
        List<IdamRoleDataRole> idamRoles = new ArrayList<>();
        Arrays.asList(roles).forEach(role -> idamRoles.add(buildIdamRoleDataRole(role)));
        return IdamRoleData.builder()
                .emailId(email)
                .activeFlag("Y")
                .deletedFlag("N")
                .roles(idamRoles).build();
    }
    
    private IdamRoleDataRole buildIdamRoleDataRole(String role) {
        return IdamRoleDataRole.builder()
                .roleName(role)
                .build();
    }

    private IdamUser buildIdamUser(String userId, String email, List<String> roleNames) {
        return IdamUser.builder().id(userId).email(email).roleNames(roleNames).build();
    }
}
