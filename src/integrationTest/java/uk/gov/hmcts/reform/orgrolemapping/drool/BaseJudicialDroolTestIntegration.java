package uk.gov.hmcts.reform.orgrolemapping.drool;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertIntegrationHelper.assertWireMockAssignmentRequests;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.APPOINTMENT_BEGIN_TIME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.APPOINTMENT_END_TIME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.adjustMapValueToDtz;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.getSidamIdsList;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.getSidamIdsSet;

public class BaseJudicialDroolTestIntegration extends BaseDroolTestIntegration {

    @SuppressWarnings("unused")
    interface JudicialIntegrationTests {

        String DISPLAY_CREATE_ORM_MAPPING_API_WITH_BOOKING
            = "#{index} - {0} - test CreateOrmMappingApi with booking - overrides: {4}";
        String DISPLAY_CREATE_ORM_MAPPING_API_WITHOUT_BOOKING
            = "#{index} - {0} - test CreateOrmMappingApi without booking - overrides: {4}";
        String DISPLAY_CREATE_ORM_MAPPING_API_WITH_EXPIRED_DATES
            = "#{index} - {0} - test CreateOrmMappingApi with expired dates - overrides: {4}";

        void testCreateOrmMappingApiWithBooking(String jrdResponseFileName,
                                                String rasRequestFileNameWithoutBooking,
                                                String rasRequestFileNameWithBooking,
                                                boolean additionalRoleTest,
                                                Map<String, String> overrideMapValues) throws Exception;

        void testCreateOrmMappingApiWithoutBooking(String jrdResponseFileName,
                                                   String rasRequestFileNameWithoutBooking,
                                                   String rasRequestFileNameWithBooking,
                                                   boolean additionalRoleTest,
                                                   Map<String, String> overrideMapValues) throws Exception;

        void testCreateOrmMappingApiWithExpiredDates(String jrdResponseFileName,
                                                     String rasRequestFileNameWithoutBooking,
                                                     String rasRequestFileNameWithBooking,
                                                     boolean additionalRoleTest,
                                                     Map<String, String> overrideMapValues) throws Exception;
    }

    public void assertCreateOrmMappingApiWithBooking(String jrdResponseFileName,
                                                     String rasRequestFileNameWithoutBooking,
                                                     String rasRequestFileNameWithBooking,
                                                     boolean additionalRoleTest,
                                                     Map<String, String> overrideMapValues,
                                                     List<FeatureFlagEnum> turnOffFlags) throws Exception {

        List<TestScenario> testScenarios = TestScenarioIntegrationHelper.generateJudicialHappyPathScenarios(
            additionalRoleTest,
            true,
            overrideMapValues
        );

        assertCreateOrmMappingApiForTestScenarios(
            testScenarios,
            jrdResponseFileName,
            rasRequestFileNameWithBooking,
            true,
            turnOffFlags
        );

    }

    @SuppressWarnings("unused") // NB: for simplicity arguments must match for all tests
    public void assertCreateOrmMappingApiWithoutBooking(String jrdResponseFileName,
                                                        String rasRequestFileNameWithoutBooking,
                                                        String rasRequestFileNameWithBooking,
                                                        boolean additionalRoleTest,
                                                        Map<String, String> overrideMapValues,
                                                        List<FeatureFlagEnum> turnOffFlags) throws Exception {

        var testScenarios = TestScenarioIntegrationHelper.generateJudicialHappyPathScenarios(
            additionalRoleTest,
            false,
            overrideMapValues
        );

        assertCreateOrmMappingApiForTestScenarios(
            testScenarios,
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            false,
            turnOffFlags
        );

    }

    @SuppressWarnings("unused") // NB: for simplicity arguments must match for all tests
    public void assertCreateOrmMappingApiWithExpiredDates(String jrdResponseFileName,
                                                          String rasRequestFileNameWithoutBooking,
                                                          String rasRequestFileNameWithBooking,
                                                          boolean additionalRoleTest,
                                                          Map<String, String> overrideMapValues,
                                                          List<FeatureFlagEnum> turnOffFlags) throws Exception {

        var testScenarios = TestScenarioIntegrationHelper.generateJudicialNegativePathScenarios(
            additionalRoleTest,
            overrideMapValues
        );

        assertCreateOrmMappingApiForTestScenarios(
            testScenarios,
            jrdResponseFileName,
            EMPTY_ROLE_ASSIGNMENT_TEMPLATE,
            true, // NB: include valid booking to prove it is ignored when other values are expired
            turnOffFlags
        );
    }

    private void assertCreateOrmMappingApiForTestScenarios(List<TestScenario> testScenarios,
                                                           String jrdResponseFileName,
                                                           String rasRequestFileName,
                                                           boolean includeBookings,
                                                           List<FeatureFlagEnum> turnOffFlags) throws Exception {
        // GIVEN
        setAllFlags(turnOffFlags);

        stubGetJudicialDetailsById(jrdResponseFileName, testScenarios);
        stubGetJudicialBookingByUserIds(testScenarios, includeBookings);
        wiremockFixtures.stubRoleAssignmentsBasicResponse(HttpStatus.CREATED);

        var expectedAssignmentRequests = getAssignmentRequestsFromFile(rasRequestFileName, testScenarios);

        // WHEN
        triggerCreateOrmMappingApi(UserType.JUDICIAL, testScenarios);

        // THEN
        assertWireMockAssignmentRequests(expectedAssignmentRequests, testScenarios);
    }

    private List<AssignmentRequest> getAssignmentRequestsFromFile(String fileName,
                                                                  List<TestScenario> testScenarios) {
        return testScenarios.stream()
            .map(testScenario -> getAssignmentRequestFromFile(fileName, testScenario))
            .collect(Collectors.toList());
    }

    private AssignmentRequest getAssignmentRequestFromFile(String fileName,
                                                           TestScenario testScenario) {
        Map<String, String> replaceMapClone = new HashMap<>(testScenario.getReplaceMap());

        // adjust date formats to DTZ
        adjustMapValueToDtz(replaceMapClone, APPOINTMENT_BEGIN_TIME, 0);
        adjustMapValueToDtz(replaceMapClone, APPOINTMENT_END_TIME, 1);

        try {
            return mapper.readValue(
                readJsonFromFile("DroolTests/Judicial/" + fileName, replaceMapClone),
                AssignmentRequest.class
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void stubGetJudicialBookingByUserIds(List<TestScenario> testScenarios,
                                                 boolean includeBookings) {
        String fileName = includeBookings ? "DroolTests/Judicial/Common/JudicialBooking" : null;
        wiremockFixtures.stubGetJudicialBookingByUserIds(
            JudicialBookingRequest.builder()
                .queryRequest(UserRequest.builder().userIds(getSidamIdsList(testScenarios)).build())
                .build(),
            "{ \"bookings\": " + readJsonArrayFromFile(fileName, testScenarios) + " }");
    }

    private void stubGetJudicialDetailsById(String fileName,
                                            List<TestScenario> testScenarios) {
        wiremockFixtures.stubGetJudicialDetailsById(
            JRDUserRequest.builder().sidamIds(getSidamIdsSet(testScenarios)).build(),
            readJsonArrayFromFile("DroolTests/Judicial/" + fileName, testScenarios)
        );
    }

}
