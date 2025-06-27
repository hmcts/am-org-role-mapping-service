package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
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
        verify(organisationService, times(1)).findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    @Test
    void findUserChangesAndInsertIntoUserRefreshQueueTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findUserChangesAndInsertIntoUserRefreshQueue();
        assertNotNull(returnedProcessMonitorDto);
        verify(professionalUserService, times(1)).findUserChangesAndInsertIntoUserRefreshQueue();
    }

}
