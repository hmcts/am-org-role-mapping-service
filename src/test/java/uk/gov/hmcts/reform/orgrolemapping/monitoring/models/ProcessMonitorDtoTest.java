package uk.gov.hmcts.reform.orgrolemapping.monitoring.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProcessMonitorDtoTest {

    private ProcessMonitorDto processMonitorDto;

    @BeforeEach
    void setUp() {
        processMonitorDto = new ProcessMonitorDto("TestProcess");
    }

    @Test
    void ctor_should_setup_defaults() {
        assertNotNull(processMonitorDto.getId());
        assertEquals("TestProcess", processMonitorDto.getProcessType());
        assertNotNull(processMonitorDto.getStartTime());
    }

    @Test
    void apply_end_result_success() {
        processMonitorDto.markAsSuccess();
        assertEquals(EndStatus.SUCCESS, processMonitorDto.getEndStatus());
        assertNull(processMonitorDto.getEndDetail());
        assertNotNull(processMonitorDto.getEndTime());
    }

    @Test
    void apply_end_result_partial_success() {
        processMonitorDto.markAsPartialSuccess("TestDetail");
        assertEquals(EndStatus.PARTIAL_SUCCESS, processMonitorDto.getEndStatus());
        assertEquals("TestDetail", processMonitorDto.getEndDetail());
        assertNotNull(processMonitorDto.getEndTime());
    }

    @Test
    void apply_end_result_failed() {
        processMonitorDto.markAsFailed("TestDetail");
        assertEquals(EndStatus.FAILED, processMonitorDto.getEndStatus());
        assertEquals("TestDetail", processMonitorDto.getEndDetail());
        assertNotNull(processMonitorDto.getEndTime());
    }
}