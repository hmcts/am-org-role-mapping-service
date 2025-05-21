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
    void findUsersWithStaleOrganisationsTest() {
        List<ProcessMonitorDto> expectedProcessMonitorDtoList = new ArrayList<>();
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));
        expectedProcessMonitorDtoList.add(mock(ProcessMonitorDto.class));

        when(organisationRefreshQueueRepository.getActiveOrganisationRefreshQueueCount())
            .thenReturn(2L, 1L, 0L);
        when(professionalUserService.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue())
            .thenReturn(expectedProcessMonitorDtoList.get(0))
            .thenReturn(expectedProcessMonitorDtoList.get(1));

        List<ProcessMonitorDto> returnedProcessMonitorDtoList = scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

        assertProcessMonitorDtoList(expectedProcessMonitorDtoList, returnedProcessMonitorDtoList);
        verify(organisationRefreshQueueRepository, times(expectedProcessMonitorDtoList.size() + 1))
            .getActiveOrganisationRefreshQueueCount();
        verify(professionalUserService, times(expectedProcessMonitorDtoList.size()))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();
    }

    private void assertProcessMonitorDtoList(List<ProcessMonitorDto> expectedProcessMonitorDtoList,
        List<ProcessMonitorDto> returnedProcessMonitorDtoList) {
        assertNotNull(returnedProcessMonitorDtoList);
        assertEquals(expectedProcessMonitorDtoList.size(), returnedProcessMonitorDtoList.size());
        for (int i = 0; i < expectedProcessMonitorDtoList.size(); i++) {
            assertEquals(expectedProcessMonitorDtoList.get(i), returnedProcessMonitorDtoList.get(i));
        }
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
