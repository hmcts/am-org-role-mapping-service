package uk.gov.hmcts.reform.orgrolemapping.scheduler;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.CaseDefinitionService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.OrganisationService;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.ProfessionalUserService;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
            "professional.role.mapping.scheduling.enabled=true",
            "professional.role.mapping.scheduling.defaultLockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.cron=0/10 * * * * *",
            "professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.findAndUpdateCaseDefinitionChanges.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.cron=1/10 * * * * *",
            "professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.findOrganisationsWithStaleProfiles.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.findOrganisationChanges.cron=2/10 * * * * *",
            "professional.role.mapping.scheduling.findOrganisationChanges.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.findOrganisationChanges.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.findUsersWithStaleOrganisations.cron=3/10 * * * * *",
            "professional.role.mapping.scheduling.findUsersWithStaleOrganisations.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.findUsersWithStaleOrganisations.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.findUserChanges.cron=4/10 * * * * *",
            "professional.role.mapping.scheduling.findUserChanges.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.findUserChanges.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.userRefresh.cron=5/10 * * * * *",
            "professional.role.mapping.scheduling.userRefresh.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.userRefresh.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.organisationRefreshCleanup.cron=6/10 * * * * *",
            "professional.role.mapping.scheduling.organisationRefreshCleanup.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.organisationRefreshCleanup.lockAtMostFor=PT30S",

            "professional.role.mapping.scheduling.userRefreshCleanup.cron=7/10 * * * * *",
            "professional.role.mapping.scheduling.userRefreshCleanup.lockAtLeastFor=PT0S",
            "professional.role.mapping.scheduling.userRefreshCleanup.lockAtMostFor=PT30S"
        }
)
class SchedulerShedLockIntegrationTest {

    private static final String LOCK_TABLE = "lock_details_provider";

    private static final List<String> EXPECTED_LOCK_NAMES = List.of(
            "PRM_organisationRefreshCleanup",
            "PRM_userRefreshCleanup",
            "PRM_Process_1_findAndUpdateCaseDefinitionChanges",
            "PRM_Process_2_findOrganisationsWithStaleProfiles",
            "PRM_Process_3_findOrganisationChanges",
            "PRM_Process_4_findUsersWithStaleOrganisations",
            "PRM_Process_5_findUserChanges",
            "PRM_Process_6_userRefresh"
    );

    private record ShedLockRecord(
            String name,
            LocalDateTime lockUntil,
            LocalDateTime lockedAt,
            String lockedBy) {
    }

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private CaseDefinitionService caseDefinitionService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private ProfessionalUserService professionalUserService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM " + LOCK_TABLE);
    }

    @Test
    void shouldExecuteAllScheduledProcessesAndVerifyShedLockEntries() throws Exception {
        LocalDateTime testStartedAt = LocalDateTime.now();

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {

                    verify(caseDefinitionService)
                            .findAndUpdateCaseDefinitionChanges();

                    verify(organisationService)
                            .findAndInsertStaleOrganisationsIntoRefreshQueue();

                    verify(organisationService)
                            .findOrganisationChangesAndInsertIntoOrganisationRefreshQueue();

                    verify(professionalUserService)
                            .findAndInsertUsersWithStaleOrganisationsIntoRefreshQueue();

                    verify(professionalUserService)
                            .findUserChangesAndInsertIntoUserRefreshQueue();

                    verify(professionalUserService)
                            .refreshUsersBatchMode();

                    verify(organisationService)
                            .deleteInactiveOrganisationRefreshRecords();

                    verify(professionalUserService)
                            .deleteInactiveUserRefreshRecords();
                });

        LocalDateTime testFinishedAt = LocalDateTime.now();

        verifyShedLockEntries(testStartedAt, testFinishedAt);
    }

    private void verifyShedLockEntries(
            LocalDateTime testStartedAt,
            LocalDateTime testFinishedAt) throws Exception {

        List<ShedLockRecord> actualLocks = jdbcTemplate.query(
                """
                SELECT
                    name,
                    lock_until,
                    locked_at,
                    locked_by
                FROM lock_details_provider
                WHERE name IN (
                    'PRM_organisationRefreshCleanup',
                    'PRM_userRefreshCleanup',
                    'PRM_Process_1_findAndUpdateCaseDefinitionChanges',
                    'PRM_Process_2_findOrganisationsWithStaleProfiles',
                    'PRM_Process_3_findOrganisationChanges',
                    'PRM_Process_4_findUsersWithStaleOrganisations',
                    'PRM_Process_5_findUserChanges',
                    'PRM_Process_6_userRefresh'
                )
                """,
                (resultSet, rowNum) -> new ShedLockRecord(
                        resultSet.getString("name"),
                        resultSet.getTimestamp("lock_until").toLocalDateTime(),
                        resultSet.getTimestamp("locked_at").toLocalDateTime(),
                        resultSet.getString("locked_by"))
        );

        assertThat(actualLocks)
                .hasSize(EXPECTED_LOCK_NAMES.size());

        assertThat(actualLocks)
                .extracting(ShedLockRecord::name)
                .containsExactlyInAnyOrderElementsOf(EXPECTED_LOCK_NAMES);

        Map<String, ShedLockRecord> locksByName = actualLocks.stream()
                .collect(Collectors.toMap(
                        ShedLockRecord::name,
                        Function.identity()));

        String expectedLockedBy =
                InetAddress.getLocalHost().getHostName();

        for (String expectedLockName : EXPECTED_LOCK_NAMES) {

            ShedLockRecord lock = locksByName.get(expectedLockName);

            assertThat(lock)
                    .as("ShedLock row for %s", expectedLockName)
                    .isNotNull();

            /*
             * 1. Verify the exact ShedLock name.
             */
            assertThat(lock.name())
                    .as("name for %s", expectedLockName)
                    .isEqualTo(expectedLockName);

            /*
             * 2. Verify locked_at was generated during this test.
             */
            assertThat(lock.lockedAt())
                    .as("locked_at for %s", expectedLockName)
                    .isBetween(testStartedAt, testFinishedAt);

            /*
             * 3. Verify lock_until is not before locked_at.
             *
             * After a successful short-running task ShedLock normally
             * releases the lock, so lock_until will be around the
             * unlock/current time rather than locked_at + lockAtMostFor.
             */
            assertThat(lock.lockUntil())
                    .as("lock_until for %s", expectedLockName)
                    .isAfterOrEqualTo(lock.lockedAt());

            /*
             * 4. Verify the exact instance that acquired the lock.
             */
            assertThat(lock.lockedBy())
                    .as("locked_by for %s", expectedLockName)
                    .isEqualTo(expectedLockedBy);
        }
    }
}

