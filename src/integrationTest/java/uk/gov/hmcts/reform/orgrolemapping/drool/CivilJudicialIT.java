package uk.gov.hmcts.reform.orgrolemapping.drool;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile.AppointmentType;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.orgrolemapping.drool.BaseJudicialDroolTestIntegration.JudicialIntegrationTests;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.generateJudicialOverrideMapValues;

class CivilJudicialIT extends BaseJudicialDroolTestIntegration implements JudicialIntegrationTests {

    public static final String SERVICE_CODES = "[[SERVICE_CODES]]";

    @SuppressWarnings("LineLength")
    static Stream<Arguments> getTestArguments() {
        return Stream.of(
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__singleRegion",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__multiRegion_1_5",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__multiRegion_1_5",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__singleRegion",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__multiRegion_1_5",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "Civil/001_Circuit Judge__Salaried__jrdResponse",
                "Civil/001_Circuit Judge__Salaried__RasAssignmentRequest__multiRegion_1_5",
                null, // no bookings
                false,
                generateJudicialOverrideMapValues(AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),

            Arguments.arguments(
                "Civil/005.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid__JrdResponse",
                "Civil/005_Deputy_District_Judge_Sitting_in_Retirement__FeePaid__RasAssignmentRequest__withoutBooking",
                "Civil/005_Deputy_District_Judge_Sitting_in_Retirement__FeePaid__RasAssignmentRequest__withBooking",
                false,
                null
            ),
            Arguments.arguments(
                "Civil/005.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid__jrdResponse",
                "Civil/005_Deputy_District_Judge_Sitting_in_Retirement__FeePaid__RasAssignmentRequest__withoutBooking",
                "Civil/005_Deputy_District_Judge_Sitting_in_Retirement__FeePaid__RasAssignmentRequest__withBooking",
                false,
                null
            )
        );
    }

    @Override
    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_CREATE_ORM_MAPPING_API_WITH_BOOKING)
    public void testCreateOrmMappingApiWithBooking(String jrdResponseFileName,
                                                   String rasRequestFileNameWithoutBooking,
                                                   String rasRequestFileNameWithBooking,
                                                   boolean additionalRoleTest,
                                                   Map<String, String> overrideMapValues) throws Exception {
        assertCreateOrmMappingApiWithBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            overrideMapValues,
            List.of() // default is all flags on
        );
    }

    @Override
    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_CREATE_ORM_MAPPING_API_WITHOUT_BOOKING)
    public void testCreateOrmMappingApiWithoutBooking(String jrdResponseFileName,
                                                      String rasRequestFileNameWithoutBooking,
                                                      String rasRequestFileNameWithBooking,
                                                      boolean additionalRoleTest,
                                                      Map<String, String> overrideMapValues) throws Exception {
        assertCreateOrmMappingApiWithoutBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            overrideMapValues,
            List.of() // default is all flags on
        );
    }

    @Override
    @MethodSource("getTestArguments")
    @ParameterizedTest(name = DISPLAY_CREATE_ORM_MAPPING_API_WITH_EXPIRED_DATES)
    public void testCreateOrmMappingApiWithExpiredDates(String jrdResponseFileName,
                                                        String rasRequestFileNameWithoutBooking,
                                                        String rasRequestFileNameWithBooking,
                                                        boolean additionalRoleTest,
                                                        Map<String, String> overrideMapValues) throws Exception {
        assertCreateOrmMappingApiWithExpiredDates(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            overrideMapValues,
            List.of() // default is all flags on
        );
    }

    @Override
    public void assertCreateOrmMappingApiWithBooking(String jrdResponseFileName,
                                                     String rasRequestFileNameWithoutBooking,
                                                     String rasRequestFileNameWithBooking,
                                                     boolean additionalRoleTest,
                                                     Map<String, String> overrideMapValues,
                                                     List<FeatureFlagEnum> turnOffFlags) throws Exception {

        // CIVIL special case - repeat all tests for both service code AAA6 and AAA7

        // first run with ServiceCode = AAA6
        super.assertCreateOrmMappingApiWithBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA6")),
            turnOffFlags
        );

        wiremockFixtures.resetRequests();

        // second run with ServiceCode = AAA7
        super.assertCreateOrmMappingApiWithBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA7")),
            turnOffFlags
        );
    }

    @Override
    public void assertCreateOrmMappingApiWithoutBooking(String jrdResponseFileName,
                                                        String rasRequestFileNameWithoutBooking,
                                                        String rasRequestFileNameWithBooking,
                                                        boolean additionalRoleTest,
                                                        Map<String, String> overrideMapValues,
                                                        List<FeatureFlagEnum> turnOffFlags) throws Exception {

        // CIVIL special case - repeat all tests for both service code AAA6 and AAA7

        // first run with ServiceCode = AAA6
        super.assertCreateOrmMappingApiWithoutBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA6")),
            turnOffFlags
        );

        wiremockFixtures.resetRequests();

        // second run with ServiceCode = AAA7
        super.assertCreateOrmMappingApiWithoutBooking(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA7")),
            turnOffFlags
        );
    }

    @Override
    public void assertCreateOrmMappingApiWithExpiredDates(String jrdResponseFileName,
                                                          String rasRequestFileNameWithoutBooking,
                                                          String rasRequestFileNameWithBooking,
                                                          boolean additionalRoleTest,
                                                          Map<String, String> overrideMapValues,
                                                          List<FeatureFlagEnum> turnOffFlags) throws Exception {

        // CIVIL special case - repeat all tests for both service code AAA6 and AAA7

        // first run with ServiceCode = AAA6
        super.assertCreateOrmMappingApiWithExpiredDates(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA6")),
            turnOffFlags
        );

        wiremockFixtures.resetRequests();

        // second run with ServiceCode = AAA7
        super.assertCreateOrmMappingApiWithExpiredDates(
            jrdResponseFileName,
            rasRequestFileNameWithoutBooking,
            rasRequestFileNameWithBooking,
            additionalRoleTest,
            cloneAndOverrideMap(overrideMapValues, Map.of(SERVICE_CODES, "AAA7")),
            turnOffFlags
        );
    }

}
