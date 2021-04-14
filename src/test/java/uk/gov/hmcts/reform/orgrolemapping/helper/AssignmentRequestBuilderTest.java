package uk.gov.hmcts.reform.orgrolemapping.helper;

import net.sourceforge.htmlunit.corejs.javascript.EmbeddedSlotMap;
import net.thucydides.junit.annotations.TestData;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_STCW;
import static uk.gov.hmcts.reform.orgrolemapping.helper.AssignmentRequestBuilder.ROLE_NAME_TCW;

class AssignmentRequestBuilderTest {

    @Test
    void buildAssignmentRequest() {
        assertNotNull(AssignmentRequestBuilder.buildAssignmentRequest(false));
    }

    @Test
    void buildRequest() {
        assertNotNull(AssignmentRequestBuilder.buildRequest(false));
    }

    @Test
    void buildRequestedRoleCollection() {
        assertTrue(AssignmentRequestBuilder.buildRequestedRoleCollection().size() >= 1);
    }

    @Test
    void buildRoleAssignment() {
        assertNotNull(AssignmentRequestBuilder.buildRoleAssignment());
        assertNotNull(AssignmentRequestBuilder.buildRoleAssignment().getAttributes());
    }

    @Test
    void buildAttributesFromFile() {
        assertNotNull(AssignmentRequestBuilder.buildAttributesFromFile("attributes.json"));
    }

    @Test
    void buildRequestedRoleForStaff() {
        assertNotNull(AssignmentRequestBuilder.buildRequestedRoleForStaff());
    }

    @Test
    void convertUserProfileToUserAccessProfile() {
        Set<CaseWorkerAccessProfile> caseWorkerAccessProfiles = AssignmentRequestBuilder
                .convertUserProfileToUserAccessProfile(TestDataBuilder
                        .buildUserProfile("21334a2b-79ce-44eb-9168-2d49a744be9c",false,"1", "2",
                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true, true, "1", "2", true));
        caseWorkerAccessProfiles.forEach(role -> {
                assertNotNull(role.getId());
                assertNotNull(role.getAreaOfWorkId());
                assertNotNull(role.getPrimaryLocationId());
                assertTrue(role.isSuspended());
                assertNotNull(role.getPrimaryLocationName());
                assertNotNull(role.getRoleId());
                assertNotNull(role.getRoleName());
                assertNotNull(role.getServiceCode());
            }
        );
        assertEquals(2, caseWorkerAccessProfiles.size());
    }

    @Test
    void convertUserProfileToJudicialAccessProfile() {

        Set<JudicialAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(TestDataBuilder.buildJudicialProfile(
                        "37395", "EMP37395",
                        "Magistrate", "Joe", "Bloggs", "Joe Bloggs", "Miss",
                        "1", "Fee Paid Judiciary 5 Days Mon - Fri",
                        "EMP62506@ejudiciary.net",
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                        "2020-04-28T16:00:49", "TRUE",
                        Collections.singletonList(TestDataBuilder.buildJPAppointment("84",
                                "5",
                                "1351",
                                "1",
                                "north-east",
                                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                                LocalDateTime.of(2020, 4, 28, 16, 1, 0),
                                "1")),
                        Collections.singletonList(TestDataBuilder.buildJPAuthorisation("52149")),
                        "Judicial"));
        judicialAccessProfiles.forEach(authorisation -> {
                assertNotNull(authorisation.getAuthorisations());
        });
        judicialAccessProfiles.forEach(appointment -> {
                assertNotNull(appointment.getUserId());
                assertNotNull(appointment.getRoleId());
                assertNotNull(appointment.getBeginTime());
                assertNotNull(appointment.getEndTime());
                assertNotNull(appointment.getRegionId());
                assertNotNull(appointment.getBaseLocationId());
                assertNotNull(appointment.getContractTypeId());
                assertNotNull(appointment.getAuthorisations());
                assertNotNull(appointment.getAppointmentId());
                }
        );
        assertEquals(1, judicialAccessProfiles.size());
    }
}