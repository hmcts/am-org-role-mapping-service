package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class SchedulerTest {

    @Mock
    private CaseDefinitionService caseDefinitionService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ProfessionalUserService professionalUserService;

    @InjectMocks
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // PRM Process 1
    @Test
    void findAndUpdateCaseDefinitionChangesTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(caseDefinitionService.findAndUpdateCaseDefinitionChanges()).thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler.findAndUpdateCaseDefinitionChanges();

        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(caseDefinitionService, times(1)).findAndUpdateCaseDefinitionChanges();
    }

    // PRM Process 2
    @Test
    void findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcessTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(organisationService.findAndInsertStaleOrganisationsIntoRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();

        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(organisationService, times(1)).findAndInsertStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 3
    @Test
    void findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcessTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(organisationService.findOrganisationChangesAndInsertIntoOrganisationRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();

        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(organisationService, times(1)).findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();
    }

    // PRM Process 4
    @Test
    void findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcessTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(professionalUserService, times(1))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    // PRM Process 5
    @Test
    void findUserChangesAndInsertIntoUserRefreshQueueTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        when(professionalUserService.findUserChangesAndInsertIntoUserRefreshQueue())
            .thenReturn(processMonitorDto);

        ProcessMonitorDto returnedProcessMonitorDto = scheduler
            .findUserChangesAndInsertIntoUserRefreshQueue();

        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(professionalUserService, times(1)).findUserChangesAndInsertIntoUserRefreshQueue();
    }

    // PRM Process 6
    @Test
    void processUserRefreshQueueTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        // arrange
        when(professionalUserService.refreshUsersBatchMode()).thenReturn(processMonitorDto);

        // act
        ProcessMonitorDto returnedProcessMonitorDto = scheduler.processUserRefreshQueue();

        // assert
        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(professionalUserService, times(1)).refreshUsersBatchMode();
    }

    // PRM Cleanup - User Refresh Queue
    @Test
    void deleteActiveUserRefreshRecordsTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        // GIVEN
        when(professionalUserService.deleteInactiveUserRefreshRecords()).thenReturn(processMonitorDto);

        // WHEN
        ProcessMonitorDto returnedProcessMonitorDto = scheduler.deleteActiveUserRefreshRecords();

        // THEN
        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(professionalUserService, times(1)).deleteInactiveUserRefreshRecords();
    }

    @Test
    void deleteActiveOrganisationRefreshRecordsTest() {
        ProcessMonitorDto processMonitorDto = mock(ProcessMonitorDto.class);

        // GIVEN
        when(organisationService.deleteInactiveOrganisationRefreshRecords()).thenReturn(processMonitorDto);

        // WHEN
        ProcessMonitorDto returnedProcessMonitorDto = scheduler.deleteActiveOrganisationRefreshRecords();

        // THEN
        assertNotNull(returnedProcessMonitorDto);
        assertEquals(processMonitorDto, returnedProcessMonitorDto);
        verify(organisationService, times(1)).deleteInactiveOrganisationRefreshRecords();
    }
}
