package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.OrganisationRefreshQueueRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

@RunWith(MockitoJUnitRunner.class)
class SchedulerTest {

    @Mock
    private CaseDefinitionService caseDefinitionService = mock(CaseDefinitionService.class);

    @Mock
    private OrganisationService organisationService = mock(OrganisationService.class);

    @Mock
    private ProfessionalUserService professionalUserService = mock(ProfessionalUserService.class);

    @Mock
    private OrganisationRefreshQueueRepository organisationRefreshQueueRepository =
        mock(OrganisationRefreshQueueRepository.class);

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new Scheduler(caseDefinitionService, organisationService, professionalUserService,
            organisationRefreshQueueRepository);
    }

    @Test
    void findUsersWithStaleOrganisationProcessTest() {
        List<OrganisationRefreshQueueEntity> organisationRefreshQueueEntities = new ArrayList<>();
        organisationRefreshQueueEntities.add(mock(OrganisationRefreshQueueEntity.class));
        organisationRefreshQueueEntities.add(mock(OrganisationRefreshQueueEntity.class));
        List<ProcessMonitorDto> expectedProcessMonitorDtoList = new ArrayList<>();
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));

        when(organisationRefreshQueueRepository.findAndLockSingleActiveOrganisationRecord())
            .thenReturn(mock(OrganisationRefreshQueueEntity.class), mock(OrganisationRefreshQueueEntity.class), null);
        when(professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(
            isA(OrganisationRefreshQueueEntity.class)))
            .thenReturn(expectedProcessMonitorDtoList.get(0))
            .thenReturn(expectedProcessMonitorDtoList.get(1));

        scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        verify(organisationRefreshQueueRepository, times(expectedProcessMonitorDtoList.size() + 1))
            .findAndLockSingleActiveOrganisationRecord();
        verify(professionalUserService, times(expectedProcessMonitorDtoList.size()))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue(isA(OrganisationRefreshQueueEntity.class));
    }

    @Test
    void findAndUpdateCaseDefinitionChangesTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(caseDefinitionService.findAndUpdateCaseDefinitionChanges()).thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler.findAndUpdateCaseDefinitionChanges();
        assertNotNull(returnedProcessMonitorDto);
        verify(caseDefinitionService, times(1)).findAndUpdateCaseDefinitionChanges();
    }

    @Test
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcessTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();
        assertNotNull(returnedProcessMonitorDto);
        verify(organisationService, times(1)).findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcessTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();
        assertNotNull(returnedProcessMonitorDto);
    }

}
