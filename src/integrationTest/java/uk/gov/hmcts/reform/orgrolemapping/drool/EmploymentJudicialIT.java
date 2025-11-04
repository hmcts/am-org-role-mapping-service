package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneTestArgumentsAndExpandOverrides;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideWhenSptwNotSupported;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_11_SCOTLAND;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_12_NATIONAL;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_NAME;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.cloneAndOverrideMap;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.expandDescription;

public class EmploymentJudicialIT {

    private static final String OUTPUT_REGION_ID = "[[OUTPUT_REGION_ID]]";

    private static final boolean NO_BOOKABLE_ROLES_FLAG = false;

    public static List<DroolJudicialTestArguments> getTestArguments() {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 President of Tribunal - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "001_President_of_Tribunal__Salaried",
                "001_PT",
                false
            )
        );

        // 002 President, Employment Tribunals (Scotland) - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_President_Employment_Tribunals_Scotland__Salaried",
                "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ",
                false
            )
        );

        // 003 Vice President - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003_Vice_President__Salaried",
                "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ",
                false
            )
        );

        // 004 Vice-President, Employment Tribunal (Scotland) - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "004_Vice_President_Employment_Tribunal_Scotland__Salaried",
                "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ",
                false
            )
        );

        // 005 Regional Employment Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "005_Regional_Employment_Judge__Salaried",
                "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ",
                false
            )
        );

        // 006 Employment Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "006_Employment_Judge__Salaried",
                "006_EJ",
                false
            )
        );

        // 007 Employment Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007_Employment_Judge__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 008 Employment Judge (Sitting in Retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.1_Employment_Judge_(Sitting_in_Retirement)__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.2_Employment_Judge_(sitting_in_retirement)__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 009 Tribunal Member - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "009_Tribunal_Member__FeePaid",
                "009_TM__010_TML"
            )
        );

        // 010 Tribunal Member Lay - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "010_Tribunal_Member_Lay__FeePaid",
                "009_TM__010_TML"
            )
        );

        // 011 Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "011_Circuit_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false
            )
        );

        // 012 High Court Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "012_High_Court_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false
            )
        );

        // 013 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013_Recorder__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 014 Regional Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "014_Regional_Tribunal_Judge__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 015 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "015_Tribunal_Judge__FeePaid",
                "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ",
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 016 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "016_Senior_Circuit_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented
                false
            )
        );

        // 017 District Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "017_District_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false
            )
        );

        // 018 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018.2_Deputy_District_Judge__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 019 Tribunal Member Disability - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "019_Tribunal_Member_Disability__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE // NB: no mapping by request of service team
            )
        );

        // 020 Tribunal Member Medical - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "020_Tribunal_Member_Medical__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE // NB: no mapping by request of service team
            )
        );

        // 021 Tribunal Member Financially Qualified - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "021_Tribunal_Member_Financially_Qualified__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE // NB: no mapping by request of service team
            )
        );

        // 022 Acting Regional Employment Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "022_Acting_Regional_Employment_Judge__Salaried",
                "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ",
                true
            )
        );

        // 023 Other Salaried & SPTW (coming in DTSAM-970)
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "023.1_Regional_Tribunal_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false
            )
        );
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "023.2_Tribunal_Judge__Salaried",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false
            )
        );

        // 024 Other Fee Paid (coming in DTSAM-970)
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "024.1_Chairman__FeePaid",
                EMPTY_ROLE_ASSIGNMENT_TEMPLATE, // NB: mapping not implemented yet: see DTSAM-970
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // expand all test arguments to cover region 12 tests
        arguments = expandTestArgumentsToCoverRegion12Tests(arguments);

        // TODO SPTW overrides to be removed in DTSAM-970
        arguments = overrideSomeSptwTestArguments(arguments);

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "Employment");
    }

    private static List<DroolJudicialTestArguments> expandTestArgumentsToCoverRegion12Tests(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArguments> outputArguments = new ArrayList<>();

        inputArguments.forEach(argument -> {
            outputArguments.add(
                cloneTestArgumentsAndExpandOverrides(
                    argument,
                    "region_2",
                    Map.of(
                        REGION_ID, REGION_02_MIDLANDS,
                        REGION_NAME, "Midlands",
                        OUTPUT_REGION_ID, REGION_02_MIDLANDS // output region should match
                    )
                )
            );
            outputArguments.add(
                cloneTestArgumentsAndExpandOverrides(
                    argument,
                    "region_12_output_11",
                    Map.of(
                        REGION_ID, REGION_12_NATIONAL,
                        REGION_NAME, "National",
                        OUTPUT_REGION_ID, REGION_11_SCOTLAND // region 12 output as 11, see DTSAM-186
                    )
                )
            );
        });

        return outputArguments;
    }

    /*
     * NB: prior to DTSAM-970: TribunalMembers only supported for locations 1036 and 1037
     */
    private static List<DroolJudicialTestArguments> generateTribunalMemberTestArguments(String jrdResponseFileName,
                                                                                        String rasRequestFileName) {

        List<DroolJudicialTestArguments> standardArguments = generateStandardFeePaidTestArguments(
            jrdResponseFileName,
            rasRequestFileName,
            false,
            NO_BOOKABLE_ROLES_FLAG
        );

        List<DroolJudicialTestArguments> outputArguments = new ArrayList<>();
        // add overrides for tribunal member tests
        standardArguments.forEach(argument -> {
            outputArguments.add(
                cloneTestArgumentsAndExpandOverrides(
                    argument,
                    "base-location-1036",
                    Map.of(
                        BASE_LOCATION_ID, "1036"
                    )
                )
            );
            outputArguments.add(
                cloneTestArgumentsAndExpandOverrides(
                    argument,
                    "base-location-1037",
                    Map.of(
                        BASE_LOCATION_ID, "1037"
                    )
                )
            );
            // custom override for base location 1038 as should return empty role assignments
            outputArguments.add(
                argument.cloneBuilder()
                    .description(
                        expandDescription(argument.getDescription(), "base-location-1038")
                    )
                    .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .overrideMapValues(
                        cloneAndOverrideMap(
                            argument.getOverrideMapValues(),
                            Map.of(
                                BASE_LOCATION_ID, "1038"
                            )
                        )
                    )
                    .build()
            );
        });

        return outputArguments;
    }

    private static List<DroolJudicialTestArguments> overrideSomeSptwTestArguments(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();

        // SPTW not supported for the following tests (to be fixed in DTSAM-970)
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("001_President_of_Tribunal__Salaried", null)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("002_President_Employment_Tribunals_Scotland__Salaried", null)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("003_Vice_President__Salaried", null)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("004_Vice_President_Employment_Tribunal_Scotland__Salaried", null)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("005_Regional_Employment_Judge__Salaried", null)
        );

        return overrideTestArguments(inputArguments, testOverrides);
    }

}
