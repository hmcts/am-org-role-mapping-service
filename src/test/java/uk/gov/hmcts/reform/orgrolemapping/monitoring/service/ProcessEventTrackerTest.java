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
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.RecordStatus;

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
    public void setUp() {
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
                .containsEntry("EndDetail", null)
                .containsEntry("RecordStatus", RecordStatus.BEING_ACTIONED.toString());


    }

    @Test
    void testTrackEventCompleted_Success() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.applyResult(EndStatus.SUCCESS, "{ 'message': 'Success' }");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.SUCCESS.toString())
                .containsEntry("EndDetail", "{ 'message': 'Success' }")
                .containsEntry("RecordStatus", RecordStatus.COMPLETED.toString());
    }

    @Test
    void testTrackEventCompleted_PartialSuccess() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.applyResult(EndStatus.PARTIAL_SUCCESS, "{ 'message': 'Partial Success' }");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.PARTIAL_SUCCESS.toString())
                .containsEntry("EndDetail", "{ 'message': 'Partial Success' }")
                .containsEntry("RecordStatus", RecordStatus.COMPLETED.toString());
    }

    @Test
    void testTrackEventCompleted_Failed() {
        ProcessMonitorDto processMonitorDto = new ProcessMonitorDto("Process 1");
        processMonitorDto.applyResult(EndStatus.FAILED, "{ 'message': 'Failed' }");
        processEventTracker.trackEventCompleted(processMonitorDto);
        verify(telemetryClient).trackEvent(anyString(), propertiesCaptor.capture(), any());
        Map<String, String> properties = propertiesCaptor.getValue();

        assertThat(properties)
                .containsEntry("Id", processMonitorDto.getId().toString())
                .containsEntry("ProcessType", "Process 1")
                .containsEntry("StartTime", processMonitorDto.getStartTime().toString())
                .containsEntry("EndTime", processMonitorDto.getEndTime().toString())
                .containsEntry("EndStatus", EndStatus.FAILED.toString())
                .containsEntry("EndDetail", "{ 'message': 'Failed' }")
                .containsEntry("RecordStatus", RecordStatus.COMPLETED.toString());
    }
}