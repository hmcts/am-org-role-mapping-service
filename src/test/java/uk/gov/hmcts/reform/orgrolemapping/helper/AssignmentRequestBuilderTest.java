package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Set<UserAccessProfile> caseWorkerAccessProfiles = AssignmentRequestBuilder
                .convertUserProfileToCaseworkerAccessProfile(TestDataBuilder
                        .buildUserProfile("21334a2b-79ce-44eb-9168-2d49a744be9c", false, "1", "2",
                                ROLE_NAME_STCW, ROLE_NAME_TCW, true, true, true, true, "1", "2", true));
        caseWorkerAccessProfiles.stream()
                .filter(obj -> obj instanceof CaseWorkerAccessProfile)
                .map(CaseWorkerAccessProfile.class::cast)
                .forEach(role -> {
                    assertNotNull(role.getId());
                    assertNotNull(role.getAreaOfWorkId());
                    assertNotNull(role.getPrimaryLocationId());
                    assertTrue(role.isSuspended());
                    assertNotNull(role.getPrimaryLocationName());
                    assertNotNull(role.getRoleId());
                    assertNotNull(role.getRoleName());
                    assertNotNull(role.getServiceCode());
                });
        assertEquals(2, caseWorkerAccessProfiles.size());
    }

    @Test
    void convertUserProfileToJudicialAccessProfile() throws IOException {
        JudicialProfile judicialProfile = TestDataBuilder.buildJudicialProfile();
        judicialProfile.getAppointments().get(0).setAppointment("1");
        judicialProfile.getAppointments().get(1).setAppointment("2");
        Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(judicialProfile);

        judicialAccessProfiles.stream()
                .filter(obj -> obj instanceof JudicialAccessProfile)
                .map(JudicialAccessProfile.class::cast)
                .forEach(appointment -> {
                    assertNotNull(appointment.getUserId());
                    assertNotNull(appointment.getBeginTime());
                    assertNotNull(appointment.getEndTime());
                    assertNotNull(appointment.getRegionId());
                    assertNotNull(appointment.getBaseLocationId());
                    assertNotNull(appointment.getTicketCodes());
                    assertEquals(2, appointment.getTicketCodes().size());
                    assertNotNull(appointment.getAppointment());
                });
        assertEquals(2, judicialAccessProfiles.size());
    }

    @Test
    void validateIACAuthorisation() {

        assertTrue(AssignmentRequestBuilder.validateAuthorisation(List.of(Authorisation.builder()
                .serviceCode("BFA1")
                .endDate(LocalDateTime.now().plusDays(1)).build()), "BFA1"));
    }

    @Test
    void validateEmptyAuthorisation() {

        List<Authorisation> authorisations = new ArrayList<>();

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(authorisations, "BFA1"));
    }

    @Test
    void validateNullAuthorisation() {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(null, "BFA1"));
    }

    @Test
    void validateNonIACAuthorisation() {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(List.of(Authorisation.builder()
                .serviceCode("BFA2")
                .endDate(LocalDateTime.now().plusDays(1)).build()), "BFA1"));
    }


    @Test
    void validateAuthorisation_inValidEndDate() {
        Authorisation authorisation = Authorisation.builder()
                .endDate(LocalDateTime.now().minusDays(2))
                .serviceCode("BFA2")
                .build();
        boolean isValidAuthorisation = AssignmentRequestBuilder.validateAuthorisation(List.of(authorisation),
                "BFA1");
        assertFalse(isValidAuthorisation);
    }

    @Test
    void validateAuthorisation_emptyList() {
        boolean authorisation = AssignmentRequestBuilder.validateAuthorisation(List.of(), "BFA1");
        assertFalse(authorisation);
    }

    @Test
    void convertUserProfileToJudicialAccessProfileWitoutAuthorisation() throws IOException {

        JudicialProfile judicialProfile = TestDataBuilder.buildJudicialProfile();
        judicialProfile.getAppointments().get(0).setAppointment("1");
        judicialProfile.getAppointments().get(1).setAppointment("2");
        judicialProfile.setAuthorisations(null);
        Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(judicialProfile);

        judicialAccessProfiles.stream()
                .filter(obj -> obj instanceof JudicialAccessProfile)
                .map(JudicialAccessProfile.class::cast)
                .forEach(appointment -> {
                    assertNotNull(appointment.getUserId());
                    assertNotNull(appointment.getBeginTime());
                    assertNotNull(appointment.getEndTime());
                    assertNotNull(appointment.getRegionId());
                    assertNotNull(appointment.getBaseLocationId());
                    assertNotNull(appointment.getTicketCodes());
                    assertEquals(0, appointment.getTicketCodes().size());
                    assertNotNull(appointment.getAppointment());
                });
        assertEquals(2, judicialAccessProfiles.size());
    }

    @Test
    void convertUserProfileToJudicialAccessProfileWithDiffTicketCode() throws IOException {
        JudicialProfile judicialProfile = TestDataBuilder.buildJudicialProfile();
        judicialProfile.getAppointments().get(0).setAppointment("1");
        judicialProfile.getAppointments().get(0).setEndDate(null);
        judicialProfile.getAppointments().get(0).setIsPrincipalAppointment("False");
        judicialProfile.getAppointments().get(1).setAppointment("2");
        judicialProfile.setAuthorisations(List.of(Authorisation.builder().ticketCode("374").build(),
                Authorisation.builder().endDate(LocalDateTime.now().plusDays(1)).build(),
                Authorisation.builder().ticketCode("373").endDate(LocalDateTime.now().minusDays(1)).build(),
                Authorisation.builder().ticketCode("372").endDate(LocalDateTime.now().plusDays(1)).build()));
        Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                .convertProfileToJudicialAccessProfile(judicialProfile);

        judicialAccessProfiles.stream()
                .filter(obj -> obj instanceof JudicialAccessProfile)
                .map(JudicialAccessProfile.class::cast)
                .forEach(appointment -> {
                    assertNotNull(appointment.getUserId());
                    assertNotNull(appointment.getBeginTime());
                    assertNotNull(appointment.getRegionId());
                    assertNotNull(appointment.getBaseLocationId());
                    assertNotNull(appointment.getTicketCodes());
                    assertEquals(2, appointment.getTicketCodes().size());
                    Assertions.assertThat(List.of("374","372")).hasSameElementsAs(appointment.getTicketCodes());
                    assertNotNull(appointment.getAppointment());
                });
        assertEquals(2, judicialAccessProfiles.size());
    }
}