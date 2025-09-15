package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import feign.FeignException;
import feign.Request;
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
import org.springframework.transaction.TransactionStatus;
import uk.gov.hmcts.reform.orgrolemapping.config.ProfessionalUserServiceConfig;
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
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.OrganisationStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService.PROCESS_4_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService.PROCESS_5_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildGetRefreshUsersResponse;
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

    private final ProfessionalRefreshOrchestrationHelper professionalRefreshOrchestrationHelper =
            Mockito.mock(ProfessionalRefreshOrchestrationHelper.class);

    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final PlatformTransactionManager transactionManager =
            Mockito.mock(PlatformTransactionManager.class);

    @Captor
    private ArgumentCaptor<ProcessMonitorDto> processMonitorDtoArgumentCaptor;

    private final ProcessEventTracker processEventTracker = Mockito.mock(ProcessEventTracker.class);

    private static final String RETRY_ONE_INTERVAL = "2";
    private static final String RETRY_TWO_INTERVAL = "15";
    private static final String RETRY_THREE_INTERVAL = "60";

    private static final ProfessionalUserServiceConfig professionalUserServiceConfig =
            new ProfessionalUserServiceConfig(RETRY_ONE_INTERVAL, RETRY_TWO_INTERVAL,
                    RETRY_THREE_INTERVAL, RETRY_ONE_INTERVAL, RETRY_TWO_INTERVAL,
                    RETRY_THREE_INTERVAL, "10", "1", "10");

    ProfessionalUserService professionalUserService = new ProfessionalUserService(
            prdService,
            accessTypesRepository,
            batchLastRunTimestampRepository,
            databaseDateTimeRepository,
            organisationRefreshQueueRepository,
            userRefreshQueueRepository,
            professionalRefreshOrchestrationHelper,
            jdbcTemplate,
            transactionManager,
            processEventTracker,
            professionalUserServiceConfig
    );

    @Nested
    @DisplayName(PROCESS_4_NAME)
    class FindAndInsertUsersWithStaleOrganisationsIntoRefreshQueue {

        @Test
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_SingleOrgEntity() {

            // GIVEN
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity("1", 1, true);

            when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(organisationRefreshQueueEntity)
                .thenReturn(null); // i.e. no more to process

            ProfessionalUser professionalUser = buildProfessionalUser(1);
            UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(1, List.of(professionalUser));
            UsersByOrganisationResponse response =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo), "1", "1", false);

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

            // THEN
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
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_MultipleOrgEntity() {

            // GIVEN
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity1
                = buildOrganisationRefreshQueueEntity("1", 1, true);
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity2
                = buildOrganisationRefreshQueueEntity("2", 1, true);

            when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(organisationRefreshQueueEntity1)
                .thenReturn(organisationRefreshQueueEntity2)
                .thenReturn(null); // i.e. no more to process

            // mock first org search
            ProfessionalUser professionalUserOrg1 = buildProfessionalUser(1);
            UsersOrganisationInfo usersOrganisationInfo1 = buildUsersOrganisationInfo(1, List.of(professionalUserOrg1));
            UsersByOrganisationRequest usersByOrganisationRequestOrg1 = new UsersByOrganisationRequest(List.of("1"));
            UsersByOrganisationResponse responseOrg1 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo1), "1", "1", false);

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), eq(usersByOrganisationRequestOrg1)))
                .thenReturn(ResponseEntity.ok(responseOrg1));

            // mock second org search
            ProfessionalUser professionalUserOrg2 = buildProfessionalUser(2);
            UsersOrganisationInfo usersOrganisationInfo2 = buildUsersOrganisationInfo(2, List.of(professionalUserOrg2));
            UsersByOrganisationRequest usersByOrganisationRequestOrg2 = new UsersByOrganisationRequest(List.of("2"));
            UsersByOrganisationResponse responseOrg2 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo2), "2", "2", false);

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), eq(usersByOrganisationRequestOrg2)))
                .thenReturn(ResponseEntity.ok(responseOrg2));

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

            // THEN
            assertNotNull(processMonitorDto);
            verify(userRefreshQueueRepository, times(2)) // i.e. 2 orgs with 1 page each
                .upsertToUserRefreshQueue(any(), any(), any());
            verify(organisationRefreshQueueRepository, times(2)) // i.e. 2 orgs
                .clearOrganisationRefreshRecord(any(), any(), any());

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_WithPaginationTest() {

            // GIVEN
            String orgId = "1";
            int accessTypesMinVersion = 2;
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity(orgId, accessTypesMinVersion, true);

            when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(organisationRefreshQueueEntity)
                .thenReturn(null); // i.e. no more to process

            // mock org page 1 search
            ProfessionalUser professionalUser1 = buildProfessionalUser(1);
            UsersOrganisationInfo usersOrganisationInfo1 = buildUsersOrganisationInfo(1, List.of(professionalUser1));
            UsersByOrganisationResponse page1 = buildUsersByOrganisationResponse(
                List.of(usersOrganisationInfo1),
                orgId, // NB: to be used in page 2 search
                professionalUser1.getUserIdentifier(), // NB: to be used in page 2 search
                true
            );

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

            // mock org page 2 search
            ProfessionalUser professionalUser2 = buildProfessionalUser(2);
            UsersOrganisationInfo usersOrganisationInfo2 = buildUsersOrganisationInfo(2, List.of(professionalUser2));
            UsersByOrganisationResponse page2 = buildUsersByOrganisationResponse(
                List.of(usersOrganisationInfo2),
                orgId, // NB: to be used in page 3 search
                professionalUser2.getUserIdentifier(), // NB: to be used in page 3 search
                true
            );

            when(prdService
                .fetchUsersByOrganisation(any(), eq(orgId), eq(professionalUser1.getUserIdentifier()), any()))
                .thenReturn(ResponseEntity.ok(page2));

            // mock org page 3 search
            ProfessionalUser professionalUser3 = buildProfessionalUser(3);
            UsersOrganisationInfo usersOrganisationInfo3 = buildUsersOrganisationInfo(2, List.of(professionalUser2));
            UsersByOrganisationResponse page3 = buildUsersByOrganisationResponse(
                List.of(usersOrganisationInfo3),
                orgId,
                professionalUser3.getUserIdentifier(),
                false // i.e. last page
            );

            when(prdService
                .fetchUsersByOrganisation(any(), eq(orgId), eq(professionalUser2.getUserIdentifier()), any()))
                .thenReturn(ResponseEntity.ok(page3));

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

            // THEN
            assertNotNull(processMonitorDto);
            verify(userRefreshQueueRepository, times(3)) // i.e. 1 org with 3 pages
                .upsertToUserRefreshQueue(any(), any(), eq(accessTypesMinVersion));
            verify(organisationRefreshQueueRepository, times(1)) // i.e. 1 org
                .clearOrganisationRefreshRecord(eq(orgId), eq(accessTypesMinVersion), any());

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_FailureMarkForRetry() {

            // GIVEN
            String orgId = "1";
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity(orgId, 1, true);

            when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(organisationRefreshQueueEntity)
                .thenReturn(null); // i.e. no more to process

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenThrow(new FeignException.InternalServerError(
                    "Fetch users failed", Mockito.mock(Request.class), null, null)
                );

            TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
            when(transactionManager.getTransaction(any()))
                .thenReturn(transactionStatus);

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

            // THEN
            assertNotNull(processMonitorDto);
            verify(userRefreshQueueRepository, times(0))
                .upsertToUserRefreshQueue(any(), any(), any());
            verify(organisationRefreshQueueRepository, times(0))
                .clearOrganisationRefreshRecord(any(), any(), any());

            verify(organisationRefreshQueueRepository, times(1))
                .updateRetry(orgId, RETRY_ONE_INTERVAL, RETRY_TWO_INTERVAL, RETRY_THREE_INTERVAL);

            verify(transactionStatus, times(1)).setRollbackOnly();

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.FAILED);
        }

        @Test
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_NoActiveRecordsTest() {

            // GIVEN
            when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(null); // i.e. none to process

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

            // THEN
            assertNotNull(processMonitorDto);
            verify(userRefreshQueueRepository, times(0))
                .upsertToUserRefreshQueue(any(), any(), any());
            verify(organisationRefreshQueueRepository, times(0))
                .clearOrganisationRefreshRecord(any(), any(), any());

            verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
            assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
        }

        @Test
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById_Found() {

            // GIVEN
            String orgId = "Org1";
            OrganisationRefreshQueueEntity organisationRefreshQueueEntity
                = buildOrganisationRefreshQueueEntity(orgId, 1, true);

            when(organisationRefreshQueueRepository.findById(orgId))
                .thenReturn(Optional.of(organisationRefreshQueueEntity));

            ProfessionalUser professionalUser = buildProfessionalUser(1);
            UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(1, List.of(professionalUser));
            UsersByOrganisationResponse response =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo), "1", "1", false);

            when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(orgId);

            // THEN
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
        void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById_NotFound() {

            // GIVEN
            String orgId = "Org1";
            when(organisationRefreshQueueRepository.findById(orgId))
                .thenReturn(Optional.empty());

            // WHEN
            ProcessMonitorDto processMonitorDto = professionalUserService
                .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(orgId);

            // THEN
            assertNotNull(processMonitorDto);
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
                .retry(0)
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

    }

    @Nested
    @DisplayName(PROCESS_5_NAME)
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
            GetRefreshUserResponse response = buildGetRefreshUsersResponse(List.of(refreshUser), "123", false);

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

            GetRefreshUserResponse response = buildGetRefreshUsersResponse(Collections.emptyList(), null, false);

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
            GetRefreshUserResponse response1 = buildGetRefreshUsersResponse(List.of(refreshUser1), "123", true);

            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));

            RefreshUser refreshUser2 = buildRefreshUser(2);
            GetRefreshUserResponse response2 = buildGetRefreshUsersResponse(List.of(refreshUser2), "456", false);

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
            GetRefreshUserResponse response = buildGetRefreshUsersResponse(List.of(refreshUser), "123", false);

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
            GetRefreshUserResponse response = buildGetRefreshUsersResponse(List.of(refreshUser), "123", false);

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

            GetRefreshUserResponse response1 =
                buildGetRefreshUsersResponse(List.of(buildRefreshUser(1)), "123", true);
            GetRefreshUserResponse response2 =
                buildGetRefreshUsersResponse(List.of(buildRefreshUser(2), buildRefreshUser(3)), "456", false);
            when(prdService.retrieveUsers(any(), any(), eq(null)))
                .thenReturn(ResponseEntity.ok(response1));
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
            GetRefreshUserResponse response = buildGetRefreshUsersResponse(List.of(refreshUser), "123", false);

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
                .status(OrganisationStatus.ACTIVE)
                .organisationLastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
        }

    }


}
