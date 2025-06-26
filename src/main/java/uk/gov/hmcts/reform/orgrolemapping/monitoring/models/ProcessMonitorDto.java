package uk.gov.hmcts.reform.orgrolemapping.monitoring.models;

import lombok.Getter;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class ProcessMonitorDto {
    private UUID id;
    private String processType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EndStatus endStatus;
    @Size(max = 8192, message = "The value cannot exceed 32768 characters")
    private String endDetail;
    private List<String> processSteps = new ArrayList<>();

    public ProcessMonitorDto(String processType) {
        this.id = UUID.randomUUID();
        this.processType = processType;
        this.startTime = LocalDateTime.now();
    }

    public void markAsSuccess() {
        applyResult(EndStatus.SUCCESS, null);
    }

    public void markAsPartialSuccess(String endDetail) {
        applyResult(EndStatus.PARTIAL_SUCCESS, endDetail);
    }

    public void markAsFailed(String endDetail) {
        applyResult(EndStatus.FAILED, endDetail);
    }

    public void addProcessStep(String step) {
        this.processSteps.add(step);
    }

    public void appendToLastProcessStep(String appendix) {
        String last = processSteps.get(processSteps.size() - 1);
        processSteps.remove(processSteps.size() - 1);
        processSteps.add(last + appendix);
    }

    /**
     * This method is used to apply the result of the process.
     * @param endStatus the end status of the process
     * @param endDetail the end detail of the process (if any, used to track partial success or failure details)
     */
    private void applyResult(EndStatus endStatus, String endDetail) {
        this.endTime = LocalDateTime.now();
        this.endStatus = endStatus;
        this.endDetail = endDetail;
    }

}
