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
    private BatchLastRunTimestampEntity batchLastRunTimestampEntity;

    @InjectMocks
    private final PrmSchedulerController controller = new PrmSchedulerController(scheduler,
        batchLastRunTimestampRepository, organisationService);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void process1Test() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process1");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findAndUpdateCaseDefinitionChanges())
            .thenReturn(processMonitorDto);

        assertEquals(response, controller.findAndUpdateCaseDefinitionChanges());
    }

    @Test
    void process2Test() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process2");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findOrganisationsWithStaleProfilesAndInsertIntoRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        assertEquals(response, controller.findOrganisationsWithStaleProfiles());
    }

    @Test
    void process3Test() {
        process3Test("2023-10-01T13:40:03");
    }

    private void process3Test(String since) {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process3");

        when(organisationService.getBatchLastRunTimestampEntity()).thenReturn(batchLastRunTimestampEntity);

        when(scheduler.findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        assertEquals(response, controller.findOrganisationChanges(since));

        verify(batchLastRunTimestampRepository,
            times(since != null ? 1 : 0)).save(batchLastRunTimestampEntity);
    }

    @Test
    void process3Test_noParam() {
        process3Test(null);
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

        assertEquals(response, controller.findUserChanges(since));

        verify(batchLastRunTimestampRepository,
            times(since != null ? 1 : 0)).save(batchLastRunTimestampEntity);
    }

    @Test
    void process5Test_noParam() {
        process5Test(null);
    }
}
