package uk.gov.hmcts.reform.orgrolemapping.drool;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.JudicialAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.drool.BaseJudicialDroolTestIntegration.JudicialIntegrationTests;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_01_LONDON;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_05_SOUTH_EAST;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.generateJudicialOverrideMapValues;

class PublicLawJudicialIT  extends BaseJudicialDroolTestIntegration implements JudicialIntegrationTests {

    /** Parameterized test arguments are.
     * * @param jrdResponseFileName - JRD response file name
     * * @param rasRequestFileNameWithoutBooking - RAS request file name without booking
     * * @param rasRequestFileNameWithBooking - RAS request file name with booking
     * * @param additionalRoleTest - boolean flag to indicate if additional role test is required
     * * @param overrideMapValues - map of values to override in the test scenario
     */
    @SuppressWarnings("checkstyle:LineLength")
    static Stream<Arguments> getTestArguments() {

        return Stream.of(

            // 001 Circuit Judge - Salaried
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "001_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 002 Deputy District Judge - Fee Paid
            Arguments.arguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withoutBooking",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withBooking",
                false,
                null
            ),


            // 003 Recorder - Fee Paid
            Arguments.arguments(
                "003_Recorder__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withoutBooking",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withBooking",
                false,
                null
            ),


            // 004 Deputy District Judge - PRFD - Fee Paid
            Arguments.arguments(
                "004_Deputy_District_Judge-PRFD__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 005 Deputy District Judge (MC) - Fee paid
            Arguments.arguments(
                "005_Deputy_District_Judge-(MC)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 006 Deputy District Judge (MC) - Sitting in Retirement - Fee paid
            Arguments.arguments(
                "006_Deputy_District_Judge-(MC)_Sitting_in_Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 007 Deputy District Judge - Fee Paid
            Arguments.arguments(
                "007.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),
            Arguments.arguments(
                "007.2_Deputy_District_Judge__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 008 Deputy District Judge - Sitting in Retirement - Fee Paid
            Arguments.arguments(
                "008.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),
            Arguments.arguments(
                "008.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 009 Deputy High Court Judge - Fee paid
            Arguments.arguments(
                "009_Deputy_High_Court_Judge__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 010 District Judge - Salaried
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "010_District_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 011 District Judge (MC) - Salaried
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "011_District_Judge-(MC)__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 012 High Court Judge - Salaried
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "012_High_Court_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 013 High Court Judge - Sitting in Retirement - Fee Paid
            Arguments.arguments(
                "013.1_High_Court_Judge_Sitting-in-Retirement__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 014 Designated Family Judge - Salaried
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__singleRegion",
                "014_DFJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__multiRegion_1_5",
                "014_DFJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__multiRegion_1_5",
                "014_DFJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__singleRegion",
                "014_DFJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__multiRegion_1_5",
                "014_DFJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "014_Designated_Family_Judge__Salaried",
                "014_DFJ__multiRegion_1_5",
                "014_DFJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 015 Magistrate - Voluntary
            Arguments.arguments(
                "015_Magistrate__Voluntary",
                "015_M__withoutBooking",
                "015_M__withoutBooking", // bookings have no effect on this scenario
                false,
                null
            ),


            // 016 Tribunal Judge - Salaried
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "016_Tribunal_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 017 Employment Judge - Salaried
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "017_Employment_Judge__Salaried",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5",
                "010_DJ__011_DJ-MC__012_HCJ__016_TJ__017_EJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 018 Specialist Circuit Judge - Salaried
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "018_Specialist_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 019 Senior Circuit Judge - Salaried
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SALARIED, REGION_05_SOUTH_EAST)
            ),
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion",
                "001_CJ__018_SpCJ__019_SeCJ__singleRegion", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_02_MIDLANDS)
            ),
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_01_LONDON)
            ),
            Arguments.arguments(
                "019_Senior_Circuit_Judge__Salaried",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5",
                "001_CJ__018_SpCJ__019_SeCJ__multiRegion_1_5", // bookings have no effect on this scenario
                false,
                generateJudicialOverrideMapValues(JudicialAccessProfile.AppointmentType.SPTW, REGION_05_SOUTH_EAST)
            ),


            // 020 Circuit Judge (sitting in retirement) - Fee Paid
            Arguments.arguments(
                "020_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withoutBooking",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withBooking",
                false,
                null
            ),


            // 021 Recorder (sitting in retirement) - Fee Paid
            Arguments.arguments(
                "021_Recorder_(sitting_in_retirement)__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withoutBooking",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withBooking",
                false,
                null
            ),


            // 022 Deputy Upper Tribunal Judge - Fee Paid
            Arguments.arguments(
                "022_Deputy_Upper_Tribunal_Judge__FeePaid",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withoutBooking",
                "002_DCJ__003_R__020_CJ-SIR__021_R-SIR__022_DUTJ__withBooking",
                false,
                null
            ),


            // 023 District Judge (MC) (sitting_in_retirement) - Fee Paid
            Arguments.arguments(
                "023_District_Judge_(MC)_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
                false,
                null
            ),


            // 024 District Judge (sitting in retirement) - Fee Paid
            Arguments.arguments(
                "024_District_Judge_(sitting_in_retirement)__FeePaid",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withoutBooking",
                "004_DDJ-PRFD__005_DDJ-MC__006_DDJ-MC-SIR__007_DDJ__008_DDJ-SIR__009_DHCJ__013_HCJ-SIR__023_DJ-MC-SIR__024_DJ-SIR__withBooking",
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
            "PublicLaw/InputFromJrd/" + jrdResponseFileName,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithoutBooking,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithBooking,
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
            "PublicLaw/InputFromJrd/" + jrdResponseFileName,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithoutBooking,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithBooking,
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
            "PublicLaw/InputFromJrd/" + jrdResponseFileName,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithoutBooking,
            "PublicLaw/OutputToRas/" + rasRequestFileNameWithBooking,
            additionalRoleTest,
            overrideMapValues,
            List.of() // default is all flags on
        );
    }
}
