package uk.gov.hmcts.reform.orgrolemapping.monitoring.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessMonitorDtoTest {

    private ProcessMonitorDto processMonitorDto;

    @BeforeEach
    void setUp() {
        processMonitorDto = new ProcessMonitorDto("TestProcess");
    }

    @Test
    void apply_end_result() {
        processMonitorDto.applyResult(EndStatus.SUCCESS, "TestDetail");
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        assertEquals("TestDetail", processMonitorDto.getEndDetail());
        assertEquals(RecordStatus.COMPLETED, processMonitorDto.getRecordStatus());
        assertNotNull(processMonitorDto.getEndTime());
    }

    @Test
    void ctor_should_setup_defaults() {
        assertNotNull(processMonitorDto.getId());
        assertEquals("TestProcess", processMonitorDto.getProcessType());
        assertEquals(RecordStatus.BEING_ACTIONED, processMonitorDto.getRecordStatus());
        assertNotNull(processMonitorDto.getStartTime());
    }
}