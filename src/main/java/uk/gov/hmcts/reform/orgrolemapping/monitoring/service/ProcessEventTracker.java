package uk.gov.hmcts.reform.orgrolemapping.monitoring.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.monitoring.models.ProcessMonitorDto;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.orgrolemapping.monitoring.models.EndStatus.SUCCESS;

@Component
public class ProcessEventTracker {
    TelemetryClient telemetryClient;

    public ProcessEventTracker(TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    public void trackEventStarted(ProcessMonitorDto processMonitorDto) {
        Map<String, String> properties = createPropertiesMap(processMonitorDto);
        String message = createMessage(processMonitorDto, "Started");

        telemetryClient.trackEvent(message, properties, null);
    }

    public void trackEventCompleted(ProcessMonitorDto processMonitorDto) {
        Map<String, String> properties = createPropertiesMap(processMonitorDto);
        String message = createMessage(processMonitorDto, "Completed");

        telemetryClient.trackEvent(message, properties, null);
    }

    private Map<String, String> createPropertiesMap(ProcessMonitorDto processMonitorDto) {
        Map<String, String> properties = new HashMap<>();
        properties.put("Id", processMonitorDto.getId().toString());
        properties.put("ProcessType", processMonitorDto.getProcessType());
        properties.put("StartTime", processMonitorDto.getStartTime().toString());
        properties.put("ProcessSteps", processMonitorDto.getProcessSteps().stream().map(Object::toString)
                .collect(Collectors.joining(", ")));
        properties.put("EndTime", processMonitorDto.getEndTime() != null
                ? processMonitorDto.getEndTime().toString() : null);
        properties.put("EndStatus",
                processMonitorDto.getEndStatus() != null
                ? processMonitorDto.getEndStatus().toString() : null);
        properties.put("EndDetail", processMonitorDto.getEndDetail());

        return properties;
    }

    private String createMessage(ProcessMonitorDto processMonitorDto, String event) {
        String message = processMonitorDto.getProcessType() + " - " + event;
        if (!event.equals("Completed")) {
            return message;
        }

        if (processMonitorDto.getEndStatus() == SUCCESS) {
            message += " - Success";
        } else if (processMonitorDto.getEndStatus() == PARTIAL_SUCCESS) {
            message += " - Partial Success";
        } else {
            message += " - Failed";
        }

        return message;
    }
}
