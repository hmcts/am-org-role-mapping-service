package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampEntity;
import uk.gov.hmcts.reform.orgrolemapping.data.BatchLastRunTimestampRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;
import uk.gov.hmcts.reform.orgrolemapping.scheduler.Scheduler;

class PrmSchedulerControllerTest {

    @Mock
    private Scheduler scheduler;

    @Mock
    private BatchLastRunTimestampRepository batchLastRunTimestampRepository;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ProfessionalUserService professionalUserService;

    @Mock
    private BatchLastRunTimestampEntity batchLastRunTimestampEntity;

    @InjectMocks
    private final PrmSchedulerController controller = new PrmSchedulerController(scheduler,
        batchLastRunTimestampRepository, organisationService, professionalUserService);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void process1Test() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process1");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findAndUpdateCaseDefinitionChanges())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findAndUpdateCaseDefinitionChanges());

        // THEN
        verify(scheduler, times(1)).findAndUpdateCaseDefinitionChanges();

    }

    @Test
    void process2Test() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process2");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findOrganisationsWithStaleProfiles());

        // THEN
        verify(scheduler, times(1)).findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess();

    }

    @Test
    void process3Test() {
        process3Test("2023-10-01T13:40:03");
    }

    private void process3Test(String since) {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process3");

        when(organisationService.getBatchLastRunTimestampEntity()).thenReturn(batchLastRunTimestampEntity);

        when(scheduler.findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findOrganisationChanges(since));

        // THEN
        verify(batchLastRunTimestampRepository,
            times(since != null ? 1 : 0)).save(batchLastRunTimestampEntity);

        verify(scheduler, times(1)).findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess();

    }

    @Test
    void process3Test_noParam() {
        process3Test(null);
    }

    @Test
    void process4Test() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process4");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler
            .findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findUsersWithStaleOrganisations(null));

        // THEN
        verify(scheduler, times(1)).findUsersWithStaleOrganisationsAndInsertIntoRefreshQueueProcess();

    }

    @Test
    void process4TestById() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process4");
        String organisationId = "1";

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(professionalUserService
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(organisationId))
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findUsersWithStaleOrganisations(organisationId));

        // THEN
        verify(professionalUserService, times(1))
            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueueById(organisationId);

    }

    @Test
    void process5Test() {
        process5Test("2023-10-01T13:40:03");
    }

    private void process5Test(String since) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process5");

        when(organisationService.getBatchLastRunTimestampEntity()).thenReturn(batchLastRunTimestampEntity);

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findUserChangesAndInsertIntoUserRefreshQueue())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.findUserChanges(since));

        // THEN
        verify(batchLastRunTimestampRepository,
            times(since != null ? 1 : 0)).save(batchLastRunTimestampEntity);

        verify(scheduler, times(1)).findUserChangesAndInsertIntoUserRefreshQueue();

    }

    @Test
    void process5Test_noParam() {
        process5Test(null);
    }

    @Test
    void process6Test() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process6");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.processUserRefreshQueue())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.processUserRefreshQueue());

        // THEN
        verify(scheduler, times(1)).processUserRefreshQueue();

    }

    @Test
    void processOrgCleanUpTest() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process6");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.deleteInactiveOrganisationRefreshRecords())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.deleteInactiveOrganisationRefreshRecords());

        // THEN
        verify(scheduler, times(1)).deleteInactiveOrganisationRefreshRecords();

    }

    @Test
    void processUserCleanUpTest() {

        // GIVEN
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process6");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.deleteInactiveUserRefreshRecords())
            .thenReturn(processMonitorDto);

        // WHEN
        assertEquals(response, controller.deleteInactiveUserRefreshRecords());

        // THEN
        verify(scheduler, times(1)).deleteInactiveUserRefreshRecords();

    }

}
