package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArgumentOverrides;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.FeatureFlagEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.cloneTestArgumentsAndExpandOverrides;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideFlagOffCatchAll;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideWhenNotSupported;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateOverrideWhenSptwNotSupported;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.overrideTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_02_MIDLANDS;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_11_SCOTLAND;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_12_NATIONAL;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestScenarioIntegrationHelper.REGION_NAME;

public class EmploymentJudicialIT {

    private static final String OUTPUT_REGION_ID = "[[OUTPUT_REGION_ID]]";

    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "002_PETS__003_VP__004_VPETS__005_REJ__022_AREJ";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "006_EJ";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "007_EJ__008_EJ-SIR__013_R__014_RTJ__015_TJ";
    private static final String TRIBUNAL_MEMBER_JUDGE_OUTPUT_TEMPLATE = "009_TM__010_TML";

    private static final boolean NO_BOOKABLE_ROLES_FLAG = false;

    public static List<DroolJudicialTestArguments> getTestArguments() {

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();

        // 001 President of Tribunal - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "001_President_of_Tribunal__Salaried",
                "001_PT" // special case as LEADERSHIP JUDGE but with no region
            )
        );

        // 002 President, Employment Tribunals (Scotland) - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "002_President_Employment_Tribunals_Scotland__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 003 Vice President - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "003_Vice_President__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 004 Vice-President, Employment Tribunal (Scotland) - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "004_Vice_President_Employment_Tribunal_Scotland__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 005 Regional Employment Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "005_Regional_Employment_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 006 Employment Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "006_Employment_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 007 Employment Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007_Employment_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 008 Employment Judge (Sitting in Retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.1_Employment_Judge_(Sitting_in_Retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008.2_Employment_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 009 Tribunal Member - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "009_Tribunal_Member__FeePaid",
                TRIBUNAL_MEMBER_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 010 Tribunal Member Lay - Fee Paid
        arguments.addAll(
            generateTribunalMemberTestArguments(
                "010_Tribunal_Member_Lay__FeePaid",
                TRIBUNAL_MEMBER_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 011 Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "011_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 012 High Court Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "012_High_Court_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 013 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 014 Regional Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "014_Regional_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 015 Tribunal Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "015_Tribunal_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // 016 Senior Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "016_Senior_Circuit_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 017 District Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "017_District_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 018 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018.2_Deputy_District_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
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
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                SALARIED_JUDGE_OUTPUT_TEMPLATE // allow AR-Expired fallback to standard salaried template
            )
        );

        // 023 Other Salaried & SPTW (coming in DTSAM-970)
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "023.1_Regional_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "023.2_Tribunal_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 024 Other Fee Paid (coming in DTSAM-970)
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "024.1_Chairman__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE,
                false,
                NO_BOOKABLE_ROLES_FLAG
            )
        );

        // expand all test arguments to cover region 12 tests
        arguments = expandTestArgumentsToCoverRegion12Tests(arguments);

        // generate extra flag off tests for EMPLOYMENT_WA_3_0
        arguments.addAll(flagOffTestsEmploymentWa30(arguments));

        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "Employment");
    }

    private static List<DroolJudicialTestArguments> flagOffTestsEmploymentWa30(
        List<DroolJudicialTestArguments> inputArguments
    ) {
        List<DroolJudicialTestArgumentOverrides> testOverrides = new ArrayList<>();
        FeatureFlagEnum flag = FeatureFlagEnum.EMPLOYMENT_WA_3_0;

        // SPTW not supported for the following tests prior to DTSAM-970
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("001_President_of_Tribunal__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("002_President_Employment_Tribunals_Scotland__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("003_Vice_President__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("004_Vice_President_Employment_Tribunal_Scotland__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenSptwNotSupported("005_Regional_Employment_Judge__Salaried", flag)
        );

        // TribunalMembers only supported for locations 1036 and 1037 prior to DTSAM-970 (i.e. so override 1038 test)
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("Location_1038_NotSupported")
            .findJrdResponseFileName("009_Tribunal_Member__FeePaid")
            .findOverrideMapValues(Map.of(
                BASE_LOCATION_ID, "1038"
            ))
            .overrideRasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("Location_1038_NotSupported")
            .findJrdResponseFileName("010_Tribunal_Member_Lay__FeePaid")
            .findOverrideMapValues(Map.of(
                BASE_LOCATION_ID, "1038"
            ))
            .overrideRasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideRasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        // the following appointments are not supported prior to DTSAM-970
        testOverrides.add(
            generateOverrideWhenNotSupported("011_Circuit_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("012_High_Court_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("016_Senior_Circuit_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("017_District_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("018.1_Deputy_District_Judge-Fee-Paid__FeePaid", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("018.2_Deputy_District_Judge__FeePaid", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("023.1_Regional_Tribunal_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("023.2_Tribunal_Judge__Salaried", flag)
        );
        testOverrides.add(
            generateOverrideWhenNotSupported("024.1_Chairman__FeePaid", flag)
        );

        // the following Additional Role Test do not need the Additional Role Fallback prior to DTSAM-970
        testOverrides.add(DroolJudicialTestArgumentOverrides.builder()
            .overrideDescription("NoGenericRoleMappingFallbackWhenAdditionalRoleIsExpired")
            .findJrdResponseFileName("022_Acting_Regional_Employment_Judge__Salaried")
            .overrideAdditionalRoleExpiredFallbackFileName(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
            .overrideTurnOffFlags(List.of(flag))
            .build()
        );

        // must use a catch-all override to run all unaffected tests with the flag off
        testOverrides.add(generateOverrideFlagOffCatchAll(flag));

        return overrideTestArguments(inputArguments, testOverrides);
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
            // create 1038 example and override for flag_off_tests
            outputArguments.add(
                cloneTestArgumentsAndExpandOverrides(
                    argument,
                    "base-location-1038",
                    Map.of(
                        BASE_LOCATION_ID, "1038"
                    )
                )
            );
        });

        return outputArguments;
    }

}
