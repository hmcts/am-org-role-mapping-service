package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.orgrolemapping.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AppointmentV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Authorisation;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AuthorisationV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.CaseWorkerProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialProfileV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleV2;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;
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

    @Nested
    @DisplayName("Convert CASEWORKER UserProfile To UserAccessProfile")
    class ConvertUserProfileToUserAccessProfile {
        @Test
        void convertUserProfileToUserAccessProfile() {
            CaseWorkerProfile caseworker = TestDataBuilder
                    .buildUserProfile("21334a2b-79ce-44eb-9168-2d49a744be9c", false, "1",
                            "2", ROLE_NAME_STCW, ROLE_NAME_TCW, true, true,
                            true, true, "1", "2", true);
            caseworker.setSkills(List.of(
                CaseWorkerProfile.Skills.builder().skillId("privatelaw").skillCode("test").description("ctsc").build(),
                CaseWorkerProfile.Skills.builder().skillId("pr").skillCode("ts").description("cts").build(),
                CaseWorkerProfile.Skills.builder().skillId("java").skillCode("junit").description("test2").build()
            ));

            Set<UserAccessProfile> caseWorkerAccessProfiles = AssignmentRequestBuilder
                    .convertUserProfileToCaseworkerAccessProfile(caseworker);
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
                        assertNotNull(role.getSkillCodes());
                        assertThat(role.getSkillCodes(), containsInAnyOrder("test", "ts", "junit"));

                    });
            assertEquals(2, caseWorkerAccessProfiles.size());

        }
    }

    @Nested
    @DisplayName("Convert JUDICIAL UserProfile To JudicialAccessProfile")
    class ConvertUserProfileToJudicialAccessProfile {

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
                        assertNotNull(appointment.getCftRegionIdV1());
                        assertNotNull(appointment.getBaseLocationId());
                        assertNotNull(appointment.getTicketCodes());
                        assertEquals(2, appointment.getTicketCodes().size());
                        assertNotNull(appointment.getAppointment());
                    });
            assertEquals(2, judicialAccessProfiles.size());
        }

        @Test
        void convertUserProfileToJudicialAccessProfileWithoutAuthorisation() throws IOException {

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
                        assertNotNull(appointment.getCftRegionIdV1());
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
                        assertNotNull(appointment.getCftRegionIdV1());
                        assertNotNull(appointment.getBaseLocationId());
                        assertNotNull(appointment.getTicketCodes());
                        assertEquals(2, appointment.getTicketCodes().size());
                        Assertions.assertThat(List.of("374","372")).hasSameElementsAs(appointment.getTicketCodes());
                        assertNotNull(appointment.getAppointment());
                    });
            assertEquals(2, judicialAccessProfiles.size());
        }

    }

    @Nested
    @DisplayName("Convert JUDICIAL V2 UserProfile To JudicialAccessProfile")
    class ConvertUserProfileToJudicialAccessProfileV2 {

        private static final String APP_1_NO_END_DATE = "App-1";
        private static final String APP_2_FUTURE_END_DATE = "App-2";
        private static final String APP_3_EXPIRED_END_DATE = "App-3";

        private static final String AUTH_1_NO_END_DATE = "Auth-1";
        private static final String AUTH_2_FUTURE_END_DATE = "Auth-2";
        private static final String AUTH_3_EXPIRED_END_DATE = "Auth-3";
        private static final String AUTH_4_EXTRA = "Auth-4";
        private static final String AUTH_5_EXTRA = "Auth-5";

        private static final String ROLE_1_NO_END_DATE = "Role-1";
        private static final String ROLE_2_FUTURE_END_DATE = "Role-2";
        private static final String ROLE_3_EXPIRED_END_DATE = "Role-3";
        private static final String ROLE_4_DUPLICATE_NAME = "Role-4";

        private static final String ROLE_NAME_1_AND_4 = "Role-Name-1-and-4";
        private static final String ROLE_NAME_2 = "Role-Name-2";
        private static final String ROLE_NAME_3 = "Role-Name-3";

        private static final String SERVICE_CODE_1 = "Service-Code-1";
        private static final String SERVICE_CODE_2 = "Service-Code-2";

        private static final String TICKET_CODE_1 = "Ticket-Code-1";
        private static final String TICKET_CODE_2 = "Ticket-Code-2";
        private static final String TICKET_CODE_3 = "Ticket-Code-3";
        private static final String TICKET_DESCRIPTION = "Ticket-Description";

        private static final String JURISDICTION = "Jurisdiction";
        private static final String JURISDICTION_ID = "Jurisdiction-ID";
        private static final String CONTRACT_TYPE_ID = "1";
        private static final String REGION_ID = "Region-ID";
        private static final String BASE_LOCATION_ID = "Base-Location-ID";
        private static final String EPIMMS_ID = "Epimms-ID";

        private AppointmentV2 buildAppointment(String appId) {

            var builder = AppointmentV2.builder()
                .appointment(appId)
                .appointmentId(appId)
                .contractTypeId(CONTRACT_TYPE_ID)
                .appointmentType("To-Be-Replaced")
                .cftRegion("To-Be-Ignored")
                .cftRegionID(REGION_ID)
                .baseLocationId(BASE_LOCATION_ID)
                .epimmsId(EPIMMS_ID)
                .startDate(LocalDate.now().minusYears(10L));
            switch (appId) {
                case APP_1_NO_END_DATE -> builder.endDate(null);
                case APP_2_FUTURE_END_DATE -> builder.endDate(LocalDate.now().plusYears(2L));
                case APP_3_EXPIRED_END_DATE -> builder.endDate(LocalDate.now().minusDays(2L));
                default -> {
                }
            }

            return builder.build();
        }

        private List<AppointmentV2> buildAppointmentsList(List<String> appIds) {
            return appIds.stream().map(this::buildAppointment).toList();
        }

        private AuthorisationV2 buildAuthorisation(String appId, String authId, String ticketCode) {

            var builder = AuthorisationV2.builder()
                .authorisationId(authId)
                .appointmentId(appId)
                .ticketCode(ticketCode)
                .ticketDescription(TICKET_DESCRIPTION)
                .jurisdictionId(JURISDICTION_ID)
                .jurisdiction(JURISDICTION)
                .startDate(LocalDate.now().minusYears(10L))
                .serviceCodes(List.of(SERVICE_CODE_1, SERVICE_CODE_2));
            switch (authId) {
                case AUTH_1_NO_END_DATE -> builder.endDate(null);
                case AUTH_2_FUTURE_END_DATE -> builder.endDate(LocalDate.now().plusYears(2L));
                case AUTH_3_EXPIRED_END_DATE -> builder.endDate(LocalDate.now().minusDays(2L));
                default -> {
                }
            }

            return builder.build();
        }

        private List<AuthorisationV2> buildAuthorisationsList(String appId,
                                                              List<String> authIds,
                                                              List<String> ticketCodes) {
            AtomicInteger counter = new AtomicInteger(0);
            return authIds.stream()
                    .map(authId -> buildAuthorisation(appId, authId, ticketCodes.get(counter.incrementAndGet() - 1)))
                    .toList();
        }

        private RoleV2 buildRole(String roleId) {

            var builder = RoleV2.builder()
                .jurisdictionRoleId(roleId)
                .startDate(LocalDate.now().minusYears(10L));
            switch (roleId) {
                case ROLE_1_NO_END_DATE ->
                    builder.endDate(null).jurisdictionRoleName(ROLE_NAME_1_AND_4);
                case ROLE_2_FUTURE_END_DATE ->
                    builder.endDate(LocalDate.now().plusYears(2L)).jurisdictionRoleName(ROLE_NAME_2);
                case ROLE_3_EXPIRED_END_DATE ->
                    builder.endDate(LocalDate.now().minusDays(2L)).jurisdictionRoleName(ROLE_NAME_3);
                case ROLE_4_DUPLICATE_NAME -> // NB: duplicate role name as ROLE_1_NO_END_DATE
                    builder.endDate(LocalDate.now().plusMonths(2L)).jurisdictionRoleName(ROLE_NAME_1_AND_4);
                case "null" ->
                    builder.jurisdictionRoleName(null);
                default -> {
                }
            }

            return builder.build();
        }

        private List<RoleV2> buildRolesList(List<String> roleIds) {
            return roleIds.stream()
                    .map(this::buildRole)
                    .toList();
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_singleAppointment() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                buildAuthorisationsList(
                        APP_1_NO_END_DATE,
                        List.of(AUTH_1_NO_END_DATE, AUTH_2_FUTURE_END_DATE),
                        List.of(TICKET_CODE_1, TICKET_CODE_2)
                )
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        assertEquals(2, accessProfile.getTicketCodes().size());
                        assertThat(accessProfile.getTicketCodes(), containsInAnyOrder(TICKET_CODE_1, TICKET_CODE_2));
                        assertEquals(2, accessProfile.getAuthorisations().size());
                    });
            assertEquals(1, judicialAccessProfiles.size());
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_multipleAppointments_doNotFilterAuthorisations() {

            // GIVEN
            var authorisationsForApp1 = buildAuthorisationsList(
                APP_1_NO_END_DATE,
                List.of(AUTH_1_NO_END_DATE),
                List.of(TICKET_CODE_1)
            );
            var authorisationsForApp2 = buildAuthorisationsList(
                APP_2_FUTURE_END_DATE,
                List.of(AUTH_2_FUTURE_END_DATE),
                List.of(TICKET_CODE_2)
            );
            List<AuthorisationV2> allAuthorisations = new ArrayList<>();
            allAuthorisations.addAll(authorisationsForApp1);
            allAuthorisations.addAll(authorisationsForApp2);
            // add extra authorisation not associated with any appointment
            allAuthorisations.add(buildAuthorisation(null, AUTH_4_EXTRA, TICKET_CODE_3));

            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE, APP_2_FUTURE_END_DATE)),
                    allAuthorisations
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, false);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: All three authorisations are included when no filter applied
                        assertEquals(3, accessProfile.getTicketCodes().size());
                        assertThat(
                                accessProfile.getTicketCodes(),
                                containsInAnyOrder(TICKET_CODE_1, TICKET_CODE_2, TICKET_CODE_3)
                        );
                        assertEquals(3, accessProfile.getAuthorisations().size());
                    });
            // NB: multiple appointments flattened into two judicialAccessProfiles
            assertEquals(2, judicialAccessProfiles.size());
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_multipleAppointments_filterAuthorisations() {

            // GIVEN
            var authorisationsForApp1 = buildAuthorisationsList(
                    APP_1_NO_END_DATE,
                    List.of(AUTH_1_NO_END_DATE),
                    List.of(TICKET_CODE_1)
            );
            var authorisationsForApp2 = buildAuthorisationsList(
                    APP_2_FUTURE_END_DATE,
                    List.of(AUTH_2_FUTURE_END_DATE),
                    List.of(TICKET_CODE_2)
            );
            List<AuthorisationV2> allAuthorisations = new ArrayList<>();
            allAuthorisations.addAll(authorisationsForApp1);
            allAuthorisations.addAll(authorisationsForApp2);
            // add extra authorisation not associated with any appointment
            allAuthorisations.add(buildAuthorisation(null, AUTH_4_EXTRA, TICKET_CODE_3));

            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE, APP_2_FUTURE_END_DATE)),
                    allAuthorisations
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: only associated (or unattached) authorisations are included when filter applied
                        assertEquals(2, accessProfile.getTicketCodes().size());
                        assertEquals(2, accessProfile.getAuthorisations().size());
                        if (APP_1_NO_END_DATE.equals(accessProfile.getAppointment())) {
                            assertThat(
                                accessProfile.getTicketCodes(),
                                containsInAnyOrder(TICKET_CODE_1, TICKET_CODE_3) // i.e. TICKET_CODE_2 is missing
                            );
                        } else {
                            assertThat(
                                    accessProfile.getTicketCodes(),
                                    containsInAnyOrder(TICKET_CODE_2, TICKET_CODE_3) // i.e. TICKET_CODE_1 is missing
                            );
                        }
                    });
            // NB: multiple appointments flattened into two judicialAccessProfiles
            assertEquals(2, judicialAccessProfiles.size());
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        void convertUserProfileToJudicialAccessProfileV2_withoutAuthorisations(boolean filterAuthorisationsByAppId) {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    null
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, filterAuthorisationsByAppId);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        assertEquals(0, accessProfile.getTicketCodes().size());
                    });
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        void convertUserProfileToJudicialAccessProfileV2_withDiffTicketCode(boolean filterAuthorisationsByAppId) {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    buildAuthorisationsList(
                            APP_1_NO_END_DATE,
                            List.of(AUTH_1_NO_END_DATE, AUTH_2_FUTURE_END_DATE, AUTH_4_EXTRA, AUTH_5_EXTRA),
                            // NB: duplicate ticket code + null should be ignored in response
                            Arrays.asList(TICKET_CODE_1, TICKET_CODE_2, TICKET_CODE_1, null)
                    )
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, filterAuthorisationsByAppId);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: removed duplicate
                        assertEquals(2, accessProfile.getTicketCodes().size());
                        assertThat(
                                accessProfile.getTicketCodes(),
                                containsInAnyOrder(TICKET_CODE_1, TICKET_CODE_2)
                        );
                    });
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        void convertUserProfileToJudicialAccessProfileV2_withInactiveTicketCode(boolean filterAuthorisationsByAppId) {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    buildAuthorisationsList(
                            APP_1_NO_END_DATE,
                            List.of(AUTH_1_NO_END_DATE, AUTH_3_EXPIRED_END_DATE),
                            List.of(TICKET_CODE_1, TICKET_CODE_2)
                    )
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, filterAuthorisationsByAppId);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: removed ticket code from expired authorisation
                        assertEquals(1, accessProfile.getTicketCodes().size());
                        assertThat(
                                accessProfile.getTicketCodes(),
                                containsInAnyOrder(TICKET_CODE_1)
                        );
                    });
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_withPrincipleLocation() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    buildAuthorisationsList(
                            APP_1_NO_END_DATE,
                            List.of(AUTH_1_NO_END_DATE),
                            List.of(TICKET_CODE_1)
                    )
            );
            // adjust relevant fields
            judicialProfile.getAppointments().get(0).setIsPrincipalAppointment("true");
            judicialProfile.getAppointments().get(0).setEpimmsId(EPIMMS_ID);

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        assertNotNull(accessProfile.getPrimaryLocationId());
                        assertEquals(EPIMMS_ID, accessProfile.getPrimaryLocationId());
                    });
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_withoutPrincipleLocation() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    buildAuthorisationsList(
                            APP_1_NO_END_DATE,
                            List.of(AUTH_1_NO_END_DATE),
                            List.of(TICKET_CODE_1)
                    )
            );
            // adjust relevant fields
            judicialProfile.getAppointments().get(0).setIsPrincipalAppointment("false");
            judicialProfile.getAppointments().get(0).setEpimmsId("ignore-me");

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        assertTrue(StringUtils.isBlank(accessProfile.getPrimaryLocationId()));
                    });
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_withDiffRoles() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    null,
                    buildRolesList(
                        // NB: duplicate role + null should be ignored in response
                        List.of(ROLE_1_NO_END_DATE, ROLE_2_FUTURE_END_DATE, ROLE_4_DUPLICATE_NAME, "null")
                    )
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: removed duplicate
                        assertEquals(2, accessProfile.getRoles().size());
                        assertThat(
                                accessProfile.getRoles(),
                                containsInAnyOrder(ROLE_NAME_1_AND_4, ROLE_NAME_2)
                        );
                    });
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_withInactiveRoles() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    null,
                    buildRolesList(List.of(ROLE_1_NO_END_DATE, ROLE_3_EXPIRED_END_DATE))
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        // NB: removed expired role
                        assertEquals(1, accessProfile.getRoles().size());
                        assertThat(
                                accessProfile.getRoles(),
                                containsInAnyOrder(ROLE_NAME_1_AND_4)
                        );
                    });
        }

        @Test
        void convertUserProfileToJudicialAccessProfileV2_withFieldMappings() {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_2_FUTURE_END_DATE)),
                    buildAuthorisationsList(
                            APP_2_FUTURE_END_DATE,
                            List.of(AUTH_2_FUTURE_END_DATE),
                            List.of(TICKET_CODE_2)
                    )
            );

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);

                        // verify accessProfile mappings
                        var inputAppointment = judicialProfile.getAppointments().get(0);
                        assertAll(
                            () -> assertEquals(judicialProfile.getSidamId(), accessProfile.getUserId()),
                            () -> assertEquals(
                                    inputAppointment.getStartDate(),
                                    accessProfile.getBeginTime().toLocalDate()
                            ),
                            () -> assertEquals(
                                    inputAppointment.getEndDate(),
                                    accessProfile.getEndTime().toLocalDate()
                            ),
                            () -> {
                                assertEquals(1, accessProfile.getTicketCodes().size());
                                assertThat(accessProfile.getTicketCodes(), containsInAnyOrder(TICKET_CODE_2));
                            },
                            () -> assertEquals(REGION_ID, accessProfile.getRegionId()),
                            () -> assertEquals(BASE_LOCATION_ID, accessProfile.getBaseLocationId()),
                            // NB: Contract Type / Appointment Type needs conversion
                            () -> assertEquals(CONTRACT_TYPE_ID, accessProfile.getContractTypeId()),
                            () -> assertEquals(AppointmentType.FEE_PAID, accessProfile.getAppointmentType())
                        );

                        // verify authorisation mappings
                        assertEquals(1, accessProfile.getAuthorisations().size());
                        var inputAuthorisation = judicialProfile.getAuthorisations().get(0);
                        var outputAuthorisation = accessProfile.getAuthorisations().get(0);
                        assertAll(
                            () -> assertEquals(
                                    inputAuthorisation.getStartDate(),
                                    outputAuthorisation.getStartDate().toLocalDate()
                            ),
                            () -> assertEquals(
                                    inputAuthorisation.getEndDate(),
                                    outputAuthorisation.getEndDate().toLocalDate()
                            ),
                            () -> assertEquals(TICKET_CODE_2, outputAuthorisation.getTicketCode()),
                            () -> assertEquals(TICKET_DESCRIPTION, outputAuthorisation.getTicketDescription()),
                            () -> assertEquals(JURISDICTION, outputAuthorisation.getJurisdiction()),
                            () -> {
                                assertEquals(2, outputAuthorisation.getServiceCodes().size());
                                assertThat(
                                        outputAuthorisation.getServiceCodes(),
                                        containsInAnyOrder(SERVICE_CODE_1, SERVICE_CODE_2)
                                );
                            }
                        );
                    });
            assertEquals(1, judicialAccessProfiles.size());
        }

        private static List<String> testParameterCsvToList(String csv) {
            List<String> output;
            switch (csv) {
                case "null" -> output = null;
                case "empty" -> output = new ArrayList<>();
                default -> output = Stream.of(csv.split("\\|"))
                        .map(item -> "null".equalsIgnoreCase(item) ? null : item)
                        .toList();
            }
            return output;
        }

        @ParameterizedTest
        @CsvSource({
            "null,1,empty",
            "empty,1,empty",
            SERVICE_CODE_1 + ",1," + SERVICE_CODE_1,
            SERVICE_CODE_1 + "|null|" + SERVICE_CODE_1 + "|,1," + SERVICE_CODE_1, // i.e. repeated, empty and null
            SERVICE_CODE_1 + "|" + SERVICE_CODE_2 + ",2," + SERVICE_CODE_1 + "|" + SERVICE_CODE_2
        })
        void convertUserProfileToJudicialAccessProfileV2_withAppointmentServiceCode(String inputServiceCodeCsv,
                                                                                    int expectedAccessProfileCount,
                                                                                    String expectedServiceCodesCsv) {

            // GIVEN
            JudicialProfileV2 judicialProfile = TestDataBuilder.buildJudicialProfileWithParamsV2(
                    buildAppointmentsList(List.of(APP_1_NO_END_DATE)),
                    buildAuthorisationsList(
                            APP_1_NO_END_DATE,
                            List.of(AUTH_1_NO_END_DATE),
                            List.of(TICKET_CODE_1)
                    )
            );
            // adjust relevant fields
            var inputServiceCodes = testParameterCsvToList(inputServiceCodeCsv);
            judicialProfile.getAppointments().get(0).setServiceCodes(inputServiceCodes);

            // WHEN
            Set<UserAccessProfile> judicialAccessProfiles = AssignmentRequestBuilder
                    .convertProfileToJudicialAccessProfileV2(judicialProfile, true);

            // THEN
            // NB: when flattening judicialProfile by each Appointment it should also flatten by each ServiceCode.
            var outputServiceCodes = new ArrayList<String>();
            assertEquals(expectedAccessProfileCount, judicialAccessProfiles.size());
            judicialAccessProfiles.stream()
                    .filter(obj -> obj instanceof JudicialAccessProfile)
                    .map(JudicialAccessProfile.class::cast)
                    .forEach(accessProfile -> {
                        assertCommonJudicialAccessProfileFields(accessProfile);
                        if (accessProfile.getServiceCode() != null) {
                            outputServiceCodes.add(accessProfile.getServiceCode());
                        }
                    });
            // verify service codes found in output
            var expectedServiceCodes = testParameterCsvToList(expectedServiceCodesCsv);
            assertEquals(expectedServiceCodes.size(), outputServiceCodes.size());
            assertThat(outputServiceCodes, containsInAnyOrder(expectedServiceCodes.toArray()));
        }

        private void assertCommonJudicialAccessProfileFields(JudicialAccessProfile accessProfile) {
            assertAll(
                    () -> assertNotNull(accessProfile.getUserId()),
                    () -> assertNotNull(accessProfile.getBeginTime()),
                    () -> assertNotNull(accessProfile.getRegionId()),
                    () -> assertNotNull(accessProfile.getCftRegionIdV1()),
                    () -> assertNotNull(accessProfile.getBaseLocationId()),
                    () -> assertNotNull(accessProfile.getTicketCodes()),
                    () -> assertNotNull(accessProfile.getAppointment()),
                    () -> accessProfile.getAuthorisations()
                            .forEach(ConvertUserProfileToJudicialAccessProfileV2::assertCommonAuthorisationFields)
            );
        }

        private static void assertCommonAuthorisationFields(Authorisation authorisation) {
            assertAll(
                    () -> assertNotNull(authorisation),
                    // NB: there are some test using ticket code set to null
                    () -> assertNotNull(authorisation.getTicketDescription()),
                    () -> assertNotNull(authorisation.getJurisdiction()),
                    () -> assertNotNull(authorisation.getStartDate()),
                    () -> assertNotNull(authorisation.getServiceCodes())
            );
        }

    }

    @Test
    void validateIACAuthorisation() {

        assertTrue(AssignmentRequestBuilder.validateAuthorisation(List.of(Authorisation.builder()
                .serviceCodes(List.of("BFA1"))
                .endDate(LocalDateTime.now().plusDays(1)).build()), "BFA1"));
    }

    @Test
    void validateEmptyAuthorisation() {

        List<Authorisation> authorisations = Collections.emptyList();

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(authorisations, "BFA1"));
    }

    @Test
    void validateNullAuthorisation() {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(null, "BFA1"));
    }

    @Test
    void validateNonIACAuthorisation() {

        assertFalse(AssignmentRequestBuilder.validateAuthorisation(List.of(Authorisation.builder()
                .serviceCodes(List.of("BFA2"))
                .endDate(LocalDateTime.now().plusDays(1)).build()), "BFA1"));
    }


    @Test
    void validateAuthorisation_inValidEndDate() {
        Authorisation authorisation = Authorisation.builder()
                .endDate(LocalDateTime.now().minusDays(2))
                .serviceCodes(List.of("BFA2"))
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

}
