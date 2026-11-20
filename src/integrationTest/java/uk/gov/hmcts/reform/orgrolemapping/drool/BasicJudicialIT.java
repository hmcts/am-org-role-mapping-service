package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_IRM_QUEUE_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;
import static uk.gov.hmcts.reform.orgrolemapping.helper.DroolJudicialTestArgumentsHelper.adjustTestArguments;

public class BasicJudicialIT {

    public static List<DroolJudicialTestArguments> getTestArguments() {
        return adjustTestArguments(
            List.of(
                DroolJudicialTestArguments.builder()
                    .jrdResponseFileName("000_UnrecognisedServiceCode")
                    .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .irmQueueFileName(EMPTY_IRM_QUEUE_TEMPLATE)
                    .additionalRoleTest(false)
                    .overrideMapValues(null)
                    .build(),
                DroolJudicialTestArguments.builder()
                    .jrdResponseFileName("001_UnrecognisedServiceCode_LastWorkingDate_Yesterday")
                    .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .irmQueueFileName(EMPTY_IRM_QUEUE_TEMPLATE)
                    .additionalRoleTest(false)
                    .overrideMapValues(null)
                    .build(),
                DroolJudicialTestArguments.builder()
                    .jrdResponseFileName("002_UnrecognisedServiceCode_LastWorkingDate_Today")
                    .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .irmQueueFileName(EMPTY_IRM_QUEUE_TEMPLATE)
                    .additionalRoleTest(false)
                    .overrideMapValues(null)
                    .build(),
                DroolJudicialTestArguments.builder()
                    .jrdResponseFileName("003_UnrecognisedServiceCode_LastWorkingDate_Tomorrow")
                    .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                    .irmQueueFileName(EMPTY_IRM_QUEUE_TEMPLATE)
                    .additionalRoleTest(false)
                    .overrideMapValues(null)
                    .build()
            ),
            "Basic"
        );
    }

}
