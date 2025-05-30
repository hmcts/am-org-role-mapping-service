package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        List<ProcessMonitorDto> expectedProcessMonitorDtoList = new ArrayList<>();
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));

        when(organisationRefreshQueueRepository.getActiveOrganisationRefreshQueueCount())
            .thenReturn(2L, 1L, 0L);
        when(professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue())
            .thenReturn(expectedProcessMonitorDtoList.get(0))
            .thenReturn(expectedProcessMonitorDtoList.get(1));

        scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        verify(organisationRefreshQueueRepository, times(expectedProcessMonitorDtoList.size() + 1))
            .getActiveOrganisationRefreshQueueCount();
        verify(professionalUserService, times(expectedProcessMonitorDtoList.size()))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    @Test
    void findUsersWithStaleOrganisationTest() {
        List<ProcessMonitorDto> expectedProcessMonitorDtos = new ArrayList<>();
        expectedProcessMonitorDtos.add(mock(ProcessMonitorDto.class));
        expectedProcessMonitorDtos.add(mock(ProcessMonitorDto.class));

        when(organisationRefreshQueueRepository.getActiveOrganisationRefreshQueueCount())
            .thenReturn(2L, 1L, 0L);
        when(professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue())
            .thenReturn(expectedProcessMonitorDtos.get(0), expectedProcessMonitorDtos.get(1));

        List<ProcessMonitorDto> actualProcessMonitorDtos = scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        assertEquals(expectedProcessMonitorDtos.size(), actualProcessMonitorDtos.size());
        assertEquals(expectedProcessMonitorDtos, actualProcessMonitorDtos);
        verify(organisationRefreshQueueRepository, times(expectedProcessMonitorDtos.size() + 1))
            .getActiveOrganisationRefreshQueueCount();
        verify(professionalUserService, times(expectedProcessMonitorDtos.size()))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
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
