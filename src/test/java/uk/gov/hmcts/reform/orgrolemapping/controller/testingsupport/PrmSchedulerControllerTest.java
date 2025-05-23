package uk.gov.hmcts.reform.orgrolemapping.controller.testingsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void setUp() {
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

        if (since != null) {
            batchLastRunTimestampRepository.save(batchLastRunTimestampEntity);
        }

        when(scheduler.findOrganisationChangesAndInsertIntoOrganisationRefreshQueueProcess())
            .thenReturn(processMonitorDto);

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        assertEquals(response, controller.findOrganisationChanges(since));
    }

    @Test
    void process3Test_noParam() {
        process3Test(null);
    }

    @Test
    void process4Test() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Test Process4");

        ResponseEntity<Object> response =
            ResponseEntity.status(HttpStatus.OK).body(processMonitorDto);

        when(scheduler.findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue())
            .thenReturn(processMonitorDto);

        assertEquals(response, controller.findUsersWithStaleOrganisations());
    }
}
