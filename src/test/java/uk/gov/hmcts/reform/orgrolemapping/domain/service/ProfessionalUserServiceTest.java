package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTime;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildProfessionalUser;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUsersOrganisationInfo;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserServiceTest {

    private final PrdService prdService = Mockito.mock(PrdService.class);

    private final AccessTypesRepository accessTypesRepository =
            Mockito.mock(AccessTypesRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            Mockito.mock(BatchLastRunTimestampRepository.class);
    private final DatabaseDateTimeRepository databaseDateTimeRepository =
            Mockito.mock(DatabaseDateTimeRepository.class);
    private final OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
            Mockito.mock(OrganisationRefreshQueueRepository.class);
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final PlatformTransactionManager transactionManager =
            Mockito.mock(PlatformTransactionManager.class);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);

    ProfessionalUserService professionalUserService = new ProfessionalUserService(
            prdService,
            accessTypesRepository,
            batchLastRunTimestampRepository,
            databaseDateTimeRepository,
            organisationRefreshQueueRepository,
            userRefreshQueueRepository,
            jdbcTemplate,
            transactionManager,
            "2",
            "15",
            "60",
            "1",
            "10",
            processEventTracker
    );

    @Test
    void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue() {
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity("1", 1, true);

        when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(organisationRefreshQueueEntity);

        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(1, List.of(professionalUser));
        UsersByOrganisationResponse response =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo), "1", "1", false);

        when(organisationRefreshQueueRepository.findById(organisationRefreshQueueEntity.getOrganisationId()))
            .thenReturn(Optional.of(organisationRefreshQueueEntity));
        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        ProcessMonitorDto processMonitorDto = professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(
                organisationRefreshQueueEntity.getOrganisationId());

        assertNotNull(processMonitorDto);
        verify(userRefreshQueueRepository, times(1))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(1))
                .clearOrganisationRefreshRecord(any(), any(), any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }
    
    @Test
    void findAndLockSingleActiveOrganisationRecordTest() {
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
            = buildOrganisationRefreshQueueEntity("1", 1, true);

        when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
            .thenReturn(organisationRefreshQueueEntity);

        OrganisationRefreshQueueEntity result = professionalUserService
            .findAndLockSingleActiveOrganisationRecord();

        assertNotNull(result);
        assertEquals(organisationRefreshQueueEntity, result);
    }

    @Test
    void findAndInsertStaleOrganisationsIntoRefreshQueue_WithPaginationTest() {
        OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity("2", 2, true);

        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(1, List.of(professionalUser));
        UsersByOrganisationResponse page1 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo), "1", "1", true);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        ProfessionalUser professionalUser2 = buildProfessionalUser(2);
        UsersOrganisationInfo usersOrganisationInfo2 = buildUsersOrganisationInfo(2, List.of(professionalUser2));
        UsersByOrganisationResponse page2 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo2), "2", "2", false);

        when(organisationRefreshQueueRepository.findById(organisationRefreshQueueEntity.getOrganisationId()))
            .thenReturn(Optional.of(organisationRefreshQueueEntity));
        when(prdService.fetchUsersByOrganisation(any(), any(String.class), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(
            organisationRefreshQueueEntity.getOrganisationId());

        verify(userRefreshQueueRepository, times(2))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(1))
                .clearOrganisationRefreshRecord(any(), any(), any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    @Test
    void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_NoActiveRecordsTest() {
        when(organisationRefreshQueueRepository.findById(null))
                .thenReturn(Optional.empty());
        when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(null);

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(
            null);
        
        verify(userRefreshQueueRepository, times(0))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(0))
                .clearOrganisationRefreshRecord(any(), any(), any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
    }

    @SuppressWarnings({"SameParameterValue"})
    private static OrganisationRefreshQueueEntity buildOrganisationRefreshQueueEntity(String organisationId,
                                                                                      Integer accessTypesMinVersion,
                                                                                      boolean active) {
        return OrganisationRefreshQueueEntity.builder()
            .organisationId(organisationId)
            .lastUpdated(LocalDateTime.now())
            .accessTypesMinVersion(accessTypesMinVersion)
            .active(active)
            .build();
    }

    private static UsersByOrganisationResponse buildUsersByOrganisationResponse(
        List<UsersOrganisationInfo> organisationInfoList,
        String lastOrgInPage,
        String lastUserInPage,
        Boolean moreAvailable) {

        return UsersByOrganisationResponse.builder()
            .organisationInfo(organisationInfoList)
            .lastOrgInPage(lastOrgInPage)
            .lastUserInPage(lastUserInPage)
            .moreAvailable(moreAvailable)
            .build();
    }

    @Nested
    @DisplayName("PRM Process 5 - Find User Changes")
    class FindUserChangesAndInsertIntoUserRefreshQueue {

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTest() {
            DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
            when(databaseDateTime.getDate()).thenReturn(mock(Instant.class));
            when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            RefreshUser refreshUser = buildRefreshUser(1);
            GetRefreshUserResponse response =
                buildRefreshUserResponse(List.of(refreshUser), "123", false);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

            ProcessMonitorDto processMonitorDto =
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

            assertNotNull(processMonitorDto);
            assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());

            verify(userRefreshQueueRepository, times(1))
                .upsertToUserRefreshQueueForLastUpdated(any(), any(), any());
            verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueue_WithNoRecordsToProcessTest() {
            DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
            when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            GetRefreshUserResponse response =
                buildRefreshUserResponse(Collections.emptyList(), null, false);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

            professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

            verify(userRefreshQueueRepository, never()).upsertToUserRefreshQueueForLastUpdated(any(), any(), any());
            verify(batchLastRunTimestampRepository, never()).save(any(BatchLastRunTimestampEntity.class));
            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus()).isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueue_WithNullResponseTest() {
            DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
            when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(null));

            professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

            verify(userRefreshQueueRepository, never()).upsertToUserRefreshQueueForLastUpdated(any(), any(), any());
            verify(batchLastRunTimestampRepository, never()).save(any(BatchLastRunTimestampEntity.class));
            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus()).isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueue_WithPaginationTest() {
            DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
            when(databaseDateTime.getDate()).thenReturn(mock(Instant.class));
            when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            RefreshUser refreshUser1 = buildRefreshUser(1);
            GetRefreshUserResponse response1 =
                buildRefreshUserResponse(List.of(refreshUser1), "123", true);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

            RefreshUser refreshUser2 = buildRefreshUser(2);
            GetRefreshUserResponse response2 =
                buildRefreshUserResponse(List.of(refreshUser2), "456", false);

            when(prdService.retrieveUsers(any(), any(), any(String.class)))
                .thenReturn(ResponseEntity.ok(response2));

            ProcessMonitorDto processMonitorDto =
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

            assertNotNull(processMonitorDto);
            assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
            verify(userRefreshQueueRepository, times(2))
                .upsertToUserRefreshQueueForLastUpdated(any(), any(), any());
            verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestWithRetrieveUsersFail() {
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            doThrow(new ServiceException("Retrieve users exception")).when(prdService)
                .retrieveUsers(any(), any(), eq(null));

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Retrieve users exception");
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestWithBatchSaveFail() {
            DatabaseDateTime databaseDateTime = mock(DatabaseDateTime.class);
            when(databaseDateTime.getDate()).thenReturn(mock(Instant.class));
            when(databaseDateTimeRepository.getCurrentTimeStamp()).thenReturn(databaseDateTime);
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            RefreshUser refreshUser = buildRefreshUser(1);
            GetRefreshUserResponse response =
                buildRefreshUserResponse(List.of(refreshUser), "123", false);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

            doThrow(new ServiceException("Batch save exception"))
                .when(batchLastRunTimestampRepository)
                .save(any());

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Batch save exception, failed at lastRecordInPage=123");
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestInsertUserFail() {
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            RefreshUser refreshUser = buildRefreshUser(1);
            GetRefreshUserResponse response =
                buildRefreshUserResponse(List.of(refreshUser), "123", false);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

            doThrow(new ServiceException("Insert exception")).when(userRefreshQueueRepository)
                .upsertToUserRefreshQueueForLastUpdated(any(), any(), any());

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Insert exception");
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestInsertUserFailOnSecond() {
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            GetRefreshUserResponse response =
                buildRefreshUserResponse(List.of(buildRefreshUser(1)), "123", true);
            GetRefreshUserResponse response2 =
                buildRefreshUserResponse(List.of(buildRefreshUser(2), buildRefreshUser(3)), "456", false);
            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));
            when(prdService.retrieveUsers(any(), any(), eq("123")))
                .thenReturn(ResponseEntity.ok(response2));

            doNothing().doThrow(new ServiceException("Insert exception")).when(userRefreshQueueRepository)
                .upsertToUserRefreshQueueForLastUpdated(any(), any(), any());

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps()).hasSize(4);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting first retrieveUsers");
            assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(1))
                .isEqualTo("attempting writeAllToUserRefreshQueue for user=1, : COMPLETED");
            assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(2))
                .isEqualTo("attempting retrieveUsers from lastRecordInPage=123");
            assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(3))
                .isEqualTo("attempting writeAllToUserRefreshQueue for user=2,user=3,");
            // should log last successful lastRecordInPage value
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Insert exception, failed at lastRecordInPage=123");
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestWithBatchServiceException() {
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
            allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
            allBatches.add(new BatchLastRunTimestampEntity(2L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 13, 34, 56, 789)));
            when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

            RefreshUser refreshUser = buildRefreshUser(1);
            GetRefreshUserResponse response =
                buildRefreshUserResponse(List.of(refreshUser), "123", false);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Single BatchLastRunTimestampEntity not found");
        }

        @Test
        void findUsersChangesAndInsertIntoRefreshQueueTestWithAccessTypesServiceException() {
            List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
            allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
            allAccessTypes.add(new AccessTypesEntity(2L, "some json"));
            when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

            Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
            );

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Single AccessTypesEntity not found");
        }

        private RefreshUser buildRefreshUser(int i) {
            return RefreshUser.builder()
                .userIdentifier("" + i)
                .lastUpdated(LocalDateTime.now())
                .organisationInfo(buildOrganisationInfo(i))
                .build();
        }

        private OrganisationInfo buildOrganisationInfo(int i) {
            return OrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
        }

        private GetRefreshUserResponse buildRefreshUserResponse(List<RefreshUser> users,
                                                                String lastRecord,
                                                                boolean moreAvailable) {
            return GetRefreshUserResponse.builder()
                .users(users)
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
        }

    }


}
