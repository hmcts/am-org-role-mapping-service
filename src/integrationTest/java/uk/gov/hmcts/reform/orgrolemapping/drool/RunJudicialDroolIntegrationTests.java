package uk.gov.hmcts.reform.orgrolemapping.drool;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JRDUserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.JudicialBookingRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.TestScenario;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.UserType;
import uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.APPOINTMENT_BEGIN_TIME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.APPOINTMENT_END_TIME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.formatJudicialTestOutputLocation;
import static uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertIntegrationHelper.assertWireMockAssignmentRequests;
import static uk.gov.hmcts.reform.orgrolemapping.helper.RoleAssignmentAssertIntegrationHelper.writeValueAsPrettyJson;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.adjustMapValueToDtz;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.getSidamIdsList;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.getSidamIdsSet;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.writeJsonToOutput;

public class RunJudicialDroolIntegrationTests extends BaseDroolTestIntegration {

    public static final String DROOL_JUDICIAL_TEST_OUTPUT_PATH = DROOL_TEST_OUTPUT_PATH + "Judicial/";
    public static final String DROOL_JUDICIAL_TEST_INDEX_FILENAME = "JudicialTestIndex.html";

    private static final String DISPLAY_NAME = "#{index} - {0}";


    static Stream<Arguments> getTestArguments() {
        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        arguments.addAll(BasicJudicialIT.getTestArguments());

        return arguments.stream()
            .map(DroolJudicialTestArguments::toArguments);
    }


    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @BeforeAll
    static void beforeAllTests() throws IOException {
        File outputDirectory = new File(DROOL_JUDICIAL_TEST_OUTPUT_PATH);
        if (outputDirectory.exists()) {
            FileUtils.deleteDirectory(outputDirectory);
        }
        outputDirectory.mkdirs();

        DroolIntegrationTestSingleton.getInstance().judicialTests.clear();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    @AfterAll
    static void afterAllTests() {
        DroolIntegrationTestSingleton.getInstance()
                .writeJudicialIndexFile(DROOL_JUDICIAL_TEST_OUTPUT_PATH);
    }

    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_NAME)
    void testCreateOrmMappingApiWithBooking(String ignoredDisplayName,
                                            DroolJudicialTestArguments testArguments) throws Exception {

        boolean includeBookingScenario = true; // NB: with booking

        writeTestArgumentsToOutput(testArguments);

        assertCreateOrmMappingApiForTestScenarios(
            DroolJudicialTestArgumentsHelper.generateJudicialHappyPathScenarios(
                testArguments,
                includeBookingScenario
            ),
            testArguments.getJrdResponseFileName(),
            testArguments.getRasRequestFileNameWithBooking(), // NB: with booking
            includeBookingScenario,
            testArguments.getTurnOffFlags()
        );

    }


    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_NAME)
    void testCreateOrmMappingApiWithoutBooking(String ignoredDisplayName,
                                               DroolJudicialTestArguments testArguments) throws Exception {

        boolean includeBookingScenario = false; // NB: without booking

        writeTestArgumentsToOutput(testArguments);

        assertCreateOrmMappingApiForTestScenarios(
            DroolJudicialTestArgumentsHelper.generateJudicialHappyPathScenarios(
                testArguments,
                includeBookingScenario
            ),
            testArguments.getJrdResponseFileName(),
            testArguments.getRasRequestFileNameWithoutBooking(), // NB: without booking
            includeBookingScenario,
            testArguments.getTurnOffFlags()
        );

    }


    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_NAME)
    void testCreateOrmMappingApiWithExpiredDates(String ignoredDisplayName,
                                                 DroolJudicialTestArguments testArguments) throws Exception {

        writeTestArgumentsToOutput(testArguments);

        assertCreateOrmMappingApiForTestScenarios(
            DroolJudicialTestArgumentsHelper.generateJudicialNegativePathScenarios(testArguments),
            testArguments.getJrdResponseFileName(),
            EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // negative test so always expect empty RAS request
            true, // NB: include valid booking to prove it is ignored when other values are expired
            testArguments.getTurnOffFlags()
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
        DroolIntegrationTestSingleton.getInstance().judicialTests.addAll(testScenarios);
        return testScenarios.stream()
            .map(testScenario -> getAssignmentRequestFromFile(fileName, testScenario))
            .toList();
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
            "{ \"bookings\": " + readJsonArrayFromFile(fileName, testScenarios, "JudicialBookingResponse") + " }");
    }

    private void stubGetJudicialDetailsById(String fileName,
                                            List<TestScenario> testScenarios) {
        wiremockFixtures.stubGetJudicialDetailsById(
            JRDUserRequest.builder().sidamIds(getSidamIdsSet(testScenarios)).build(),
            readJsonArrayFromFile("DroolTests/Judicial/" + fileName, testScenarios, "JrdUserResponse")
        );
    }

    private void writeTestArgumentsToOutput(DroolJudicialTestArguments testArguments) throws JsonProcessingException {
        writeJsonToOutput(
            writeValueAsPrettyJson(testArguments),
            formatJudicialTestOutputLocation(testArguments, ""),
            "TestArguments"
        );
    }

}
