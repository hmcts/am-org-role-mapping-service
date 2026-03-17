package uk.gov.hmcts.reform.orgrolemapping.domain.model;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.Jurisdiction;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.crd.JobTitle;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.orgrolemapping.helper.TestDataBuilder.buildUserAccessProfile;

class CaseWorkerAccessProfileTest {

    @Nested
    @DisplayName("HasValidJobTitle Tests")
    class HasValidJobTitle {

        @ParameterizedTest
        @EnumSource(value = JobTitle.class)
        void testHasValidJobTitle(JobTitle jobTitle) {

            JobTitle nonMatchingJobTitle1 = jobTitle.equals(JobTitle.SENIOR_LEGAL_CASEWORKER)
                ? JobTitle.LEGAL_CASEWORKER : JobTitle.SENIOR_LEGAL_CASEWORKER;

            JobTitle nonMatchingJobTitle2 = jobTitle.equals(JobTitle.HEARING_CENTRE_TEAM_LEADER)
                ? JobTitle.HEARING_CENTRE_ADMIN : JobTitle.HEARING_CENTRE_TEAM_LEADER;


            // happy path tests - matching JobTitle.roleId

            testHasValidValidJobTitle(
                List.of(jobTitle),
                jobTitle.getRoleId(),
                true, // happy path test
                "should match on valid roleId: " + jobTitle.getRoleId() + " (single JobTitle)"
            );
            testHasValidValidJobTitle(
                List.of(nonMatchingJobTitle1, jobTitle, nonMatchingJobTitle2),
                jobTitle.getRoleId(),
                true, // happy path test
                "should match on valid roleId: " + jobTitle.getRoleId() + " (multiple JobTitles)"
            );


            // negative tests - non-matching code

            testHasValidValidJobTitle(
                List.of(jobTitle),
                null,
                false, // as no match
                "should fail to match on null roleId (single JobTitle)"
            );
            testHasValidValidJobTitle(
                List.of(nonMatchingJobTitle1, jobTitle, nonMatchingJobTitle2),
                null,
                false, // as no match
                "should fail to match on null roleId (multiple JobTitles)"
            );
            testHasValidValidJobTitle(
                List.of(jobTitle),
                "",
                false, // as no match
                "should fail to match on empty roleId (single JobTitle)"
            );
            testHasValidValidJobTitle(
                List.of(nonMatchingJobTitle1, jobTitle, nonMatchingJobTitle2),
                "",
                false, // as no match
                "should fail to match on empty roleId (multiple JobTitles)"
            );
            testHasValidValidJobTitle(
                List.of(jobTitle),
                "wrong",
                false, // as no match
                "should fail to match on wrong roleId (single JobTitle)"
            );
            testHasValidValidJobTitle(
                List.of(nonMatchingJobTitle1, jobTitle, nonMatchingJobTitle2),
                "wrong",
                false, // as no match
                "should fail to match on wrong roleId (multiple JobTitles)"
            );

        }


        void testHasValidValidJobTitle(List<JobTitle> jobTitle,
                                       String roleId,
                                       boolean expectedHasValidServiceCode,
                                       String testDescription) {

            // GIVEN
            CaseWorkerAccessProfile accessProfile = buildUserAccessProfile(roleId, "any_service_code", false);

            // WHEN
            boolean result = accessProfile.hasValidJobTitle(jobTitle.toArray(new JobTitle[0]));

            // THEN
            assertEquals(expectedHasValidServiceCode, result, testDescription);

        }

    }

    @Nested
    @DisplayName("HasValidServiceCode Tests")
    class HasValidServiceCode {


        @ParameterizedTest
        @EnumSource(value = Jurisdiction.class)
        void testHasValidServiceCode(Jurisdiction jurisdiction) {

            assertFalse(CollectionUtils.isEmpty(jurisdiction.getServiceCodes()), "Jurisdiction has no codes defined");


            // happy path tests - matching Jurisdiction.ServiceCode

            jurisdiction.getServiceCodes().forEach(serviceCode ->
                testHasValidServiceCode(
                    jurisdiction,
                    serviceCode,
                    true, // happy path test
                    "should match on valid serviceCode: " + serviceCode
                )
            );


            // negative tests - non-matching Jurisdiction.ServiceCode

            testHasValidServiceCode(
                jurisdiction,
                null,
                false, // as no match
                "should fail to match on null serviceCode"
            );
            testHasValidServiceCode(
                jurisdiction,
                "",
                false, // as no match
                "should fail to match on empty serviceCode"
            );
            testHasValidServiceCode(
                jurisdiction,
                "wrong",
                false, // as no match
                "should fail to match on wrong serviceCode"
            );

        }

        void testHasValidServiceCode(Jurisdiction jurisdiction,
                                     String serviceCode,
                                     boolean expectedHasValidServiceCode,
                                     String testDescription) {

            // GIVEN
            CaseWorkerAccessProfile accessProfile = buildUserAccessProfile("any_role_id", serviceCode, false);

            // WHEN
            boolean result = accessProfile.hasValidServiceCode(jurisdiction);

            // THEN
            assertEquals(expectedHasValidServiceCode, result, testDescription);

        }

    }

    @Nested
    @DisplayName("isCaseAllocator Tests")
    class IsCaseAllocatorTests {
        @org.junit.jupiter.api.Test
        void shouldReturnTrueWhenCaseAllocatorFlagIsY() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .caseAllocatorFlag("Y")
                .build();
            org.junit.jupiter.api.Assertions.assertTrue(profile.isCaseAllocator(), "Should return true when caseAllocatorFlag is 'Y'");
        }

        @org.junit.jupiter.api.Test
        void shouldReturnFalseWhenCaseAllocatorFlagIsN() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .caseAllocatorFlag("N")
                .build();
            org.junit.jupiter.api.Assertions.assertFalse(profile.isCaseAllocator(), "Should return false when caseAllocatorFlag is not 'Y'");
        }

        @org.junit.jupiter.api.Test
        void shouldReturnFalseWhenCaseAllocatorFlagIsNull() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .caseAllocatorFlag(null)
                .build();
            org.junit.jupiter.api.Assertions.assertFalse(profile.isCaseAllocator(), "Should return false when caseAllocatorFlag is null");
        }
    }

    @Nested
    @DisplayName("isTaskSupervisor Tests")
    class IsTaskSupervisorTests {
        @org.junit.jupiter.api.Test
        void shouldReturnTrueWhenTaskSupervisorFlagIsY() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .taskSupervisorFlag("Y")
                .build();
            org.junit.jupiter.api.Assertions.assertTrue(profile.isTaskSupervisor(), "Should return true when taskSupervisorFlag is 'Y'");
        }

        @org.junit.jupiter.api.Test
        void shouldReturnFalseWhenTaskSupervisorFlagIsN() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .taskSupervisorFlag("N")
                .build();
            org.junit.jupiter.api.Assertions.assertFalse(profile.isTaskSupervisor(), "Should return false when taskSupervisorFlag is not 'Y'");
        }

        @org.junit.jupiter.api.Test
        void shouldReturnFalseWhenTaskSupervisorFlagIsNull() {
            CaseWorkerAccessProfile profile = CaseWorkerAccessProfile.builder()
                .taskSupervisorFlag(null)
                .build();
            org.junit.jupiter.api.Assertions.assertFalse(profile.isTaskSupervisor(), "Should return false when taskSupervisorFlag is null");
        }
    }
}
