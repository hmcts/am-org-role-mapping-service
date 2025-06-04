package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.AccessTypesRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTime;
import uk.gov.hmcts.reform.orgrolemapping.data.DatabaseDateTimeRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.GetRefreshUserResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RefreshUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalUserServiceTest {

    private final PrdService prdService = mock(PrdService.class);
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final AccessTypesRepository accessTypesRepository = mock(AccessTypesRepository.class);
    private final DatabaseDateTimeRepository databaseDateTimeRepository = mock(DatabaseDateTimeRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            mock(BatchLastRunTimestampRepository.class);
    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    ProfessionalUserService professionalUserService  = new ProfessionalUserService(
            prdService,
            userRefreshQueueRepository,
            "1",
            jdbcTemplate,
            accessTypesRepository, batchLastRunTimestampRepository, databaseDateTimeRepository,
            processEventTracker, "10");

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
                buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

        ProcessMonitorDto processMonitorDto =
            professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        verify(userRefreshQueueRepository, times(1))
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
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
                buildRefreshUserResponse(refreshUser1, "123", true);

        when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

        RefreshUser refreshUser2 = buildRefreshUser(2);
        GetRefreshUserResponse response2 =
                buildRefreshUserResponse(refreshUser2, "456", false);

        when(prdService.retrieveUsers(any(), any(), any(String.class)))
                .thenReturn(ResponseEntity.ok(response2));

        ProcessMonitorDto processMonitorDto =
            professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        assertNotNull(processMonitorDto);
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        verify(userRefreshQueueRepository, times(2))
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
    }

    @Test
    void findUsersChangesAndInsertIntoRefreshQueueTestWithRetreiveUsersFail() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);

        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

        RefreshUser refreshUser = buildRefreshUser(1);
        GetRefreshUserResponse response =
                buildRefreshUserResponse(refreshUser, "123", false);

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
                buildRefreshUserResponse(refreshUser, "123", false);

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
                buildRefreshUserResponse(refreshUser, "123", false);

        when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));

        doThrow(new ServiceException("Insert exception")).when(userRefreshQueueRepository)
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());

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

        GetRefreshUserResponse response = buildRefreshUserResponse(buildRefreshUser(1), "123", true);
        GetRefreshUserResponse response2 = buildRefreshUsersResponse(buildRefreshUser(2),
                buildRefreshUser(3), "456", false);
        when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response));
        when(prdService.retrieveUsers(any(), any(), eq("123")))
                .thenReturn(ResponseEntity.ok(response2));

        doNothing().doThrow(new ServiceException("Insert exception")).when(userRefreshQueueRepository)
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());

        Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
        );

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().size())
                .isEqualTo(4);
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(0))
                .isEqualTo("attempting first retrieveUsers");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(1))
                .isEqualTo("attempting writeAllToUserRefreshQueue for user=1, : COMPLETED");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(2))
                .isEqualTo("attempting retrieveUsers from lastRecordInPage=123");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getProcessSteps().get(3))
                .isEqualTo("attempting writeAllToUserRefreshQueue for user=2,user=3,");
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndDetail())
                .isEqualTo("Insert exception, failed at lastRecordInPage=456");
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
                buildRefreshUserResponse(refreshUser, "123", false);

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

    private GetRefreshUserResponse buildRefreshUserResponse(RefreshUser user,
                                                            String lastRecord,
                                                            boolean moreAvailable) {
        return GetRefreshUserResponse.builder()
                .users(List.of(user))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }

    private GetRefreshUserResponse buildRefreshUsersResponse(RefreshUser user1, RefreshUser user2,
                                                            String lastRecord,
                                                            boolean moreAvailable) {
        return GetRefreshUserResponse.builder()
                .users(List.of(user1, user2))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }
}

