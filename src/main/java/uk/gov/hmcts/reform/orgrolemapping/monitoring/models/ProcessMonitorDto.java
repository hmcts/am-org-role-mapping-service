package uk.gov.hmcts.reform.orgrolemapping.monitoring.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProcessMonitorDto {
    private UUID id;
    private String processType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EndStatus endStatus;
    private String endDetail;
    private RecordStatus recordStatus;

    public ProcessMonitorDto(String processType) {
        this.id = UUID.randomUUID();
        this.processType = processType;
        this.startTime = LocalDateTime.now();
        this.recordStatus = RecordStatus.BEING_ACTIONED;
    }

    /**
     * This method is used to apply the result of the process.
     * @param endStatus the end status of the process
     * @param endDetail the end detail of the process (if any, used to track partial success or failure details)
     */
    public void applyResult(EndStatus endStatus, String endDetail) {
        this.endTime = LocalDateTime.now();
        this.recordStatus = RecordStatus.COMPLETED;
        this.endStatus = endStatus;
        this.endDetail = endDetail;
    }
}

