package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.data.UserRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUser;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersByOrganisationResponse;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UsersOrganisationInfo;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.service.ProcessEventTracker;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfessionalUserServiceTest {

    private final PrdService prdService = Mockito.mock(PrdService.class);
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
            organisationRefreshQueueRepository,
            userRefreshQueueRepository,
            jdbcTemplate,
            transactionManager,
            "2",
            "15",
            "60",
            "1",
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

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(response));

        ProcessMonitorDto processMonitorDto = professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(organisationRefreshQueueEntity);

        assertNotNull(processMonitorDto);
        verify(userRefreshQueueRepository, times(1))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any(), any());

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
                = buildOrganisationRefreshQueueEntity("1", 1, true);

        ProfessionalUser professionalUser = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo = buildUsersOrganisationInfo(1, List.of(professionalUser));
        UsersByOrganisationResponse page1 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo), "1", "1", true);

        when(prdService.fetchUsersByOrganisation(any(), eq(null), eq(null), any()))
                .thenReturn(ResponseEntity.ok(page1));

        ProfessionalUser professionalUser2 = buildProfessionalUser(1);
        UsersOrganisationInfo usersOrganisationInfo2 = buildUsersOrganisationInfo(1, List.of(professionalUser2));
        UsersByOrganisationResponse page2 =
                buildUsersByOrganisationResponse(List.of(usersOrganisationInfo2), "1", "1", false);

        when(prdService.fetchUsersByOrganisation(any(), any(String.class), any(String.class), any()))
                .thenReturn(ResponseEntity.ok(page2));

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(
            organisationRefreshQueueEntity);

        verify(userRefreshQueueRepository, times(2))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(1))
                .setActiveFalse(any(), any(), any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    @Test
    void findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue_NoActiveRecordsTest() {
        when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
                .thenReturn(null);

        professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(
            null);
        
        verify(userRefreshQueueRepository, times(0))
                .upsertToUserRefreshQueue(any(), any(), any());
        verify(organisationRefreshQueueRepository, times(0))
                .setActiveFalse(any(), any(), any());

        verify(processEventTracker).trackEventCompleted(processMonitorDtoArgumentCaptor.capture());
        assertThat(processMonitorDtoArgumentCaptor.getValue().getEndStatus())
                .isEqualTo(EndStatus.SUCCESS);
    }

    public static OrganisationRefreshQueueEntity buildOrganisationRefreshQueueEntity(String organisationId,
                                                                               Integer accessTypesMinVersion,
                                                                               boolean active) {
        return OrganisationRefreshQueueEntity.builder()
                .organisationId(organisationId)
                .lastUpdated(LocalDateTime.now())
                .accessTypesMinVersion(accessTypesMinVersion)
                .active(active)
                .build();
    }

    public static UsersOrganisationInfo buildUsersOrganisationInfo(int i, List<ProfessionalUser> users) {
        return UsersOrganisationInfo.builder()
                .organisationIdentifier("" + i)
                .status("ACTIVE")
                .organisationProfileIds(List.of("SOLICITOR_PROFILE"))
                .users(users)
                .build();
    }

    public static ProfessionalUser buildProfessionalUser(int i) {
        return ProfessionalUser.builder()
                .userIdentifier("" + i)
                .firstName("fName " + i)
                .lastName("lName " + i)
                .email("user" + i + "@mail.com")
                .lastUpdated(LocalDateTime.now())
                .deleted(LocalDateTime.now())
                .build();
    }

    public static UsersByOrganisationResponse buildUsersByOrganisationResponse(
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
