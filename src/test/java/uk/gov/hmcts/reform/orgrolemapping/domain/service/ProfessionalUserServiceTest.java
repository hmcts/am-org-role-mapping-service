package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.task.UserInfo;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.orgrolemapping.data.*;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.*;
import uk.gov.hmcts.reform.orgrolemapping.helper.UserBuilder;


import java.sql.Ref;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfessionalUserServiceTest {

    private final PrdService prdService = mock(PrdService.class);
    private final UserRefreshQueueRepository userRefreshQueueRepository =
            Mockito.mock(UserRefreshQueueRepository.class);
    private final NamedParameterJdbcTemplate jdbcTemplate =
            Mockito.mock(NamedParameterJdbcTemplate.class);
    private final AccessTypesRepository accessTypesRepository = mock(AccessTypesRepository.class);
    private final DatabaseDateTimeRepository databaseDateTimeRepository = mock(DatabaseDateTimeRepository.class);
    private final BatchLastRunTimestampRepository batchLastRunTimestampRepository =
            mock(BatchLastRunTimestampRepository.class);
    ProfessionalUserService professionalUserService  = new ProfessionalUserService(
            prdService,
            userRefreshQueueRepository,
            "1",
            jdbcTemplate,
            accessTypesRepository, batchLastRunTimestampRepository, databaseDateTimeRepository, "10"
    );

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

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        verify(userRefreshQueueRepository, times(1))
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
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

        professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue();

        verify(userRefreshQueueRepository, times(2))
                .insertIntoUserRefreshQueueForLastUpdated(any(), any(), any());
        verify(batchLastRunTimestampRepository, times(1)).save(any(BatchLastRunTimestampEntity.class));
    }

    @Test
    void findUserChangesAndInsertIntoUserRefreshQueue_WithBatchServiceExceptionTest() {
        List<AccessTypesEntity> allAccessTypes = new ArrayList<>();
        allAccessTypes.add(new AccessTypesEntity(1L, "some json"));
        when(accessTypesRepository.findAll()).thenReturn(allAccessTypes);
        List<BatchLastRunTimestampEntity> allBatches = new ArrayList<>();
        allBatches.add(new BatchLastRunTimestampEntity(1L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        allBatches.add(new BatchLastRunTimestampEntity(2L, LocalDateTime.of(2023, 12, 30, 0, 0, 0, 0),
                LocalDateTime.of(2023, 12, 31, 12, 34, 56, 789)));
        when(batchLastRunTimestampRepository.findAll()).thenReturn(allBatches);

        Assertions.assertThrows(ServiceException.class, () ->
                professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue()
        );
    }

    private RefreshUser buildRefreshUser (int i) {
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
                .lastUpdated(LocalDateTime.now())
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .build();
    }

    private GetRefreshUserResponse buildRefreshUserResponse (RefreshUser user,
                                                            String lastRecord,
                                                             boolean moreAvailable) {
        return GetRefreshUserResponse.builder()
                .users(List.of(user))
                .lastRecordInPage(lastRecord)
                .moreAvailable(moreAvailable)
                .build();
    }
}

