package uk.gov.hmcts.reform.orgrolemapping.monitoring.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

class ProcessEventTrackerTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Captor
    private ArgumentCaptor<Map<String, String>> propertiesCaptor;

    private ProcessEventTracker processEventTracker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        processEventTracker = new ProcessEventTracker(telemetryClient);
    }

    @Test
    void testTrackEventStarted() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processEventTracker.trackEventStarted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", null)
                .containsEntry("EndStatus", null)
                .containsEntry("EndDetail", null);


    }

    @Test
    void testTrackEventCompleted_Success() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.markAsSuccess();
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.SUCCESS.toString())
                .containsEntry("EndDetail", null);
    }

    @Test
    void testTrackEventCompleted_PartialSuccess() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.markAsPartialSuccess("{ 'message': 'Partial Success' }");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.PARTIAL_SUCCESS.toString())
                .containsEntry("EndDetail", "{ 'message': 'Partial Success' }");
    }

    @Test
    void testTrackEventCompleted_Failed() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.markAsFailed("{ 'message': 'Failed' }");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.FAILED.toString())
                .containsEntry("ProcessSteps", "")
                .containsEntry("EndDetail", "{ 'message': 'Failed' }");
    }

    @Test
    void testTrackEventCompleted_SuccessWithProcessSteps() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.markAsSuccess();
        processMonitorDto.addProcessStep("Step 1");
        processMonitorDto.addProcessStep("Step 2");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("ProcessSteps", "Step 1, Step 2")
                .containsEntry("EndStatus", EndStatus.SUCCESS.toString())
                .containsEntry("EndDetail", null);
    }

}
