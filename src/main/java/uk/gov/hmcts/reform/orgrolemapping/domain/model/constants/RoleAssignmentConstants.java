package uk.gov.hmcts.reform.orgrolemapping.domain.model.constants;

import lombok.experimental.UtilityClass;

/**
 * Constants related to Role Assignments.
 * These constants are primarily used in the Drool validation rules.
 */
@UtilityClass
public final class RoleAssignmentConstants {

    @UtilityClass
    public static final class Attributes {

        @UtilityClass
        public static final class Name {

            public static final String BASE_LOCATION = "baseLocation";
            public static final String BOOKABLE = "bookable";
            public static final String CONTRACT_TYPE = "contractType";
            public static final String JURISDICTION = "jurisdiction";
            public static final String PRIMARY_LOCATION = "primaryLocation";
            public static final String REGION = "region";
            public static final String WORK_TYPES = "workTypes";

        }

        @UtilityClass
        public static final class ContractType {

            public static final String FEE_PAID = "Fee-Paid";
            public static final String SALARIED = "Salaried";
            public static final String VOLUNTARY = "Voluntary";

        }

    }

    @UtilityClass
    public static final class RoleName {

        // COMMON
        public static final String CASE_ALLOCATOR = "case-allocator";
        public static final String TASK_SUPERVISOR = "task-supervisor";

        // HEARINGS
        public static final String HEARING_MANAGER = "hearing-manager";
        public static final String HEARING_VIEWER = "hearing-viewer";

        // JUDICIAL
        public static final String FEE_PAID_JUDGE = "fee-paid-judge";
        public static final String JUDGE = "judge";
        public static final String LEADERSHIP_JUDGE = "leadership-judge";
        public static final String MAGISTRATE = "magistrate";
        public static final String TRIBUNAL_MEMBER = "tribunal-member";

        // HMCTS
        public static final String HMCTS_ADMIN = "hmcts-admin";
        public static final String HMCTS_CTSC = "hmcts-ctsc";
        public static final String HMCTS_JUDICIARY = "hmcts-judiciary";
        public static final String HMCTS_LEGAL_OPERATIONS = "hmcts-legal-operations";

        // SPECIFIC ACCESS
        public static final String SPECIFIC_ACCESS_APPROVER_ADMIN = "specific-access-approver-admin";
        public static final String SPECIFIC_ACCESS_APPROVER_CTSC = "specific-access-approver-ctsc";
        public static final String SPECIFIC_ACCESS_APPROVER_JUDICIARY = "specific-access-approver-judiciary";
        public static final String SPECIFIC_ACCESS_APPROVER_LEGAL_OPS = "specific-access-approver-legal-ops";

    }

}
