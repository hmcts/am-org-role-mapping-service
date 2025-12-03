package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardFeePaidTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardSalariedTestArguments;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.generateStandardVoluntaryTestArguments;

public class PrivateLawJudicialIT {


    private static final String LEADERSHIP_JUDGE_OUTPUT_TEMPLATE = "Salaried_Leadership_Judge";
    private static final String SALARIED_JUDGE_OUTPUT_TEMPLATE = "Salaried_Judge";
    private static final String SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "Salaried_Circuit_Judge";
    private static final String FEE_PAID_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Judge";
    private static final String FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE = "FeePaid_Circuit_Judge";
    private static final String MAGISTRATE_OUTPUT_TEMPLATE = "Magistrate";

    private static final String HEARING_ROLES_ONLY_OUTPUT_TEMPLATE = "HearingRolesOnly";

    public static List<DroolJudicialTestArguments> getTestArguments() {

        // PrivateLaw special tests:
        // * Additional Role tests will use a fallback when the Additional Role has expired as the hearing role
        //   mappings are based on any active Appointment + Authorisation being present

        List<DroolJudicialTestArguments> arguments = new ArrayList<>();


        // 001 Circuit Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "001_Circuit_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 002 Deputy Circuit Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "002_Deputy_Circuit_Judge__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 003 Deputy District Judge - PRFD - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "003_Deputy_District_Judge-PRFD__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 004 Deputy District Judge (MC) - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "004_Deputy_District_Judge-(MC)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 005 Deputy District Judge (MC) - Sitting in Retirement - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "005_Deputy_District_Judge-(MC)_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 006 Deputy District Judge - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006.1_Deputy_District_Judge-Fee-Paid__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "006.2_Deputy_District_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 007 Deputy District Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.1_Deputy_District_Judge_Sitting_in_Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "007.2_Deputy_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 008 Deputy High Court Judge - Fee paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "008_Deputy_High_Court_Judge__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 009 District Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "009_District_Judge__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 010 District Judge (MC) - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "010_District_Judge-(MC)__Salaried",
                SALARIED_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 011 High Court Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "011_High_Court_Judge__Salaried",
                SALARIED_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 012 High Court Judge - Sitting in Retirement - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "012.1_High_Court_Judge_Sitting-in-Retirement__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 013 Recorder - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "013_Recorder__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 014 Designated Family Judge - Salaried
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "014_Designated_Family_Judge__Salaried",
                LEADERSHIP_JUDGE_OUTPUT_TEMPLATE,
                HEARING_ROLES_ONLY_OUTPUT_TEMPLATE // hearing fallback needed on this additional role test
            )
        );


        // 015 Family Division Liaison Judge (Presiding Judge - Salaried)
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "015_Family_Division_Liaison_Judge",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                HEARING_ROLES_ONLY_OUTPUT_TEMPLATE // hearing fallback needed on this additional role test
            )
        );


        // 016 Senior Family Liaison Judge (Resident Judge - Salaried)
        arguments.addAll(
            generateStandardSalariedTestArguments(
                "016_Senior_Family_Liaison_Judge",
                SALARIED_JUDGE_OUTPUT_TEMPLATE,
                HEARING_ROLES_ONLY_OUTPUT_TEMPLATE // hearing fallback needed on this additional role test
            )
        );


        // 017 Magistrate - Voluntary
        arguments.addAll(
            generateStandardVoluntaryTestArguments(
                "017_Magistrate__Voluntary",
                MAGISTRATE_OUTPUT_TEMPLATE,
                false // NB: bookings have no effect on this scenario
            )
        );


        // 018 District Judge (MC) (sitting_in_retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "018_District_Judge_(MC)_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // 019 District Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "019_District_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_JUDGE_OUTPUT_TEMPLATE
            )
        );

        // 020 FL401-Judge – Fee Paid :: see Civil tests
        // 021 FL401-Judge – Salaried :: see Civil tests

        // 022 Circuit Judge (sitting in retirement) - Fee Paid
        arguments.addAll(
            generateStandardFeePaidTestArguments(
                "022_Circuit_Judge_(sitting_in_retirement)__FeePaid",
                FEE_PAID_CIRCUIT_JUDGE_OUTPUT_TEMPLATE
            )
        );


        // adjust test arguments ready for use
        return adjustTestArguments(arguments, "PrivateLaw");
    }

}
