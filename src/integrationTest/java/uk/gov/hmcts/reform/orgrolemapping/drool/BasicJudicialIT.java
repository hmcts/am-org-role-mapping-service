package uk.gov.hmcts.reform.orgrolemapping.drool;

import uk.gov.hmcts.reform.orgrolemapping.domain.model.DroolJudicialTestArguments;

import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.drool.BaseDroolTestIntegration.EMPTY_ROLE_ASSIGNMENT_TEMPLATE;

public class BasicJudicialIT {

    public static List<DroolJudicialTestArguments> getTestArguments() {
        return List.of(
            DroolJudicialTestArguments.builder()
                .description("Basic: Unrecognised ServiceCode")
                .outputLocation("Basic/000_UnrecognisedServiceCode/")
                .jrdResponseFileName("Basic/InputFromJrd/000_UnrecognisedServiceCode")
                .rasRequestFileNameWithBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                .rasRequestFileNameWithoutBooking(EMPTY_ROLE_ASSIGNMENT_TEMPLATE)
                .additionalRoleTest(false)
                .overrideMapValues(null)
                .build()
        );
    }

}
