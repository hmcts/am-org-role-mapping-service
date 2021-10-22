package uk.gov.hmcts.reform.orgrolemapping.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void buildAttributesFromFile_InvalidRequest() {
        assertThrows(InvalidRequest.class, () ->
                AssignmentRequestBuilder.buildAttributesFromFile(""));
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
    void convertUserProfileToJudicialAccessProfile() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile judicialProfile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                JudicialProfile.class);
        judicialProfile.getAppointments().get(0).setAppointment("1");
        judicialProfile.getAppointments().get(1).setAppointment("2");
        Set<JudicialAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(judicialProfile);

        judicialAccessProfiles.forEach(appointment -> {
                assertNotNull(appointment.getUserId());
                assertNotNull(appointment.getBeginTime());
                assertNotNull(appointment.getEndTime());
                assertNotNull(appointment.getRegionId());
                assertNotNull(appointment.getBaseLocationId());
                assertNotNull(appointment.getTicketCodes());
                assertEquals(2, appointment.getTicketCodes().size());
                assertNotNull(appointment.getAppointment());
            }
        );
        assertEquals(2, judicialAccessProfiles.size());
    }

    @Test
    void validateIACAuthorisation()  {

        assertTrue(AssignmentRequestBuilder.validateAuthorisation(Arrays.asList(Authorisation.builder()
                .serviceCode("BFA1")
                .endDate(LocalDateTime.now().plusDays(1)).build())));
    }

    @Test
    void validateEmptyAuthorisation()  {

        List<Authorisation> authorisations = new ArrayList<>();

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(authorisations));
    }

    @Test
    void validateNullAuthorisation()  {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(null));
    }

    @Test
    void validateNonIACAuthorisation()  {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(Arrays.asList(Authorisation.builder()
                .serviceCode("BFA2")
                .endDate(LocalDateTime.now().plusDays(1)).build())));
    }

    @Test
    void convertUserProfileToJudicialAccessProfileWitoutAuthorisation() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        JudicialProfile judicialProfile =
                objectMapper.readValue(new File("src/main/resources/judicialProfileSample.json"),
                        JudicialProfile.class);
        judicialProfile.getAppointments().get(0).setAppointment("1");
        judicialProfile.getAppointments().get(1).setAppointment("2");
        judicialProfile.setAuthorisations(null);
        Set<JudicialAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(judicialProfile);

        judicialAccessProfiles.forEach(appointment -> {
                assertNotNull(appointment.getUserId());
                assertNotNull(appointment.getBeginTime());
                assertNotNull(appointment.getEndTime());
                assertNotNull(appointment.getRegionId());
                assertNotNull(appointment.getBaseLocationId());
                assertNotNull(appointment.getTicketCodes());
                assertEquals(1, appointment.getTicketCodes().size());
                assertNotNull(appointment.getAppointment());
            }
        );
        assertEquals(2, judicialAccessProfiles.size());
    }
}
