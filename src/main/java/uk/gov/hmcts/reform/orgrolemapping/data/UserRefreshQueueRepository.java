package uk.gov.hmcts.reform.orgrolemapping.data;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES_MIN_VERSION;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.DELETED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_PROFILE_IDS;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_STATUS;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.USER_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.USER_LAST_UPDATED;

@Repository
public interface UserRefreshQueueRepository extends JpaRepository<UserRefreshQueueEntity, String> {

    // NB: This upsert is for PRM Process 5 & Process 6 Single User:
    //     on conflict it will only update record when this is a fresh update.
    String UPSERT_SQL_WHEN_LAST_UPDATED = """
            insert into user_refresh_queue (user_id, user_last_updated, access_types_min_version, deleted,
                                            access_types, organisation_id, organisation_status,
                                            organisation_profile_ids, active)
            values (:userId, :userLastUpdated, :accessTypesMinVersion, :deleted, CAST(:accessTypes AS jsonb),
                    :organisationId, :organisationStatus, string_to_array(:organisationProfileIds, ','), true)
            on conflict (user_id) do update
            set
                access_types_min_version = greatest(excluded.access_types_min_version,
                                                    user_refresh_queue.access_types_min_version),
                user_last_updated = excluded.user_last_updated,
                last_updated = now(),
                active = true,
                deleted = excluded.deleted,
                access_types = excluded.access_types,
                organisation_id = excluded.organisation_id,
                organisation_status = excluded.organisation_status,
                organisation_profile_ids  = excluded.organisation_profile_ids
            where excluded.last_updated > user_refresh_queue.last_updated
            """;

    String SKIP_LOCKED = "-2";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserRefreshQueueEntity findByUserId(String userId);

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserRefreshQueueEntity> findFirstByActiveTrue();

    @Modifying
    @Query(value = UPSERT_SQL_WHEN_LAST_UPDATED, nativeQuery = true)
    void upsert(String userId, LocalDateTime userLastUpdated, Long accessTypesMinVersion, LocalDateTime deleted,
                String accessTypes, String organisationId, String organisationStatus,
                String organisationProfileIds);

    default void upsertToUserRefreshQueue(NamedParameterJdbcTemplate jdbcTemplate,
                                          List<ProfessionalUserData> rows,
                                          Integer accessTypeMinVersion) {

        // NB: This upsert is for PRM Process 4:
        //     on conflict it will always set active to true even when user is unchanged.

        String sql = """
            insert into user_refresh_queue (user_id, user_last_updated, access_types_min_version, deleted,
                                            access_types, organisation_id, organisation_status,
                                            organisation_profile_ids, active)
            values (:userId, :userLastUpdated, :accessTypesMinVersion, :deleted, CAST(:accessTypes AS jsonb),
                    :organisationId, :organisationStatus, string_to_array(:organisationProfileIds, ','), true)
            on conflict (user_id) do update
            set
                access_types_min_version = greatest(excluded.access_types_min_version,
                                                    user_refresh_queue.access_types_min_version),
                user_last_updated = greatest(excluded.user_last_updated, user_refresh_queue.user_last_updated),
                last_updated = now(),
                active = true,
                deleted = case
                    when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.deleted
                    else user_refresh_queue.deleted end,
                access_types = case
                    when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.access_types
                    else user_refresh_queue.access_types end,
                organisation_id = case
                    when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.organisation_id
                    else user_refresh_queue.organisation_id end,
                organisation_status = case
                    when excluded.user_last_updated > user_refresh_queue.user_last_updated then
                    excluded.organisation_status else user_refresh_queue.organisation_status end,
                organisation_profile_ids = case
                    when excluded.user_last_updated > user_refresh_queue.user_last_updated then
                    excluded.organisation_profile_ids else user_refresh_queue.organisation_profile_ids end
            """;

        jdbcTemplate.batchUpdate(sql, getParamsFromProfessionalUserDataRows(rows, accessTypeMinVersion));
    }

    default void upsertToUserRefreshQueueForLastUpdated(NamedParameterJdbcTemplate jdbcTemplate,
                                                        List<ProfessionalUserData> rows,
                                                        Integer accessTypeMinVersion) {
        jdbcTemplate.batchUpdate(
            UPSERT_SQL_WHEN_LAST_UPDATED,
            getParamsFromProfessionalUserDataRows(rows, accessTypeMinVersion)
        );
    }

    private MapSqlParameterSource[] getParamsFromProfessionalUserDataRows(List<ProfessionalUserData> rows,
                                                                          Integer accessTypeMinVersion) {
        return rows.stream().filter(r -> r.getUserId() !=  null)
            .map(r -> {
                MapSqlParameterSource paramValues = new MapSqlParameterSource();
                paramValues.addValue(USER_ID, r.getUserId());
                paramValues.addValue(USER_LAST_UPDATED, r.getUserLastUpdated());
                paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
                paramValues.addValue(DELETED, r.getDeleted());
                paramValues.addValue(ACCESS_TYPES, r.getAccessTypes());
                paramValues.addValue(ORGANISATION_ID, r.getOrganisationId());
                paramValues.addValue(ORGANISATION_STATUS, r.getOrganisationStatus());
                paramValues.addValue(ORGANISATION_PROFILE_IDS, r.getOrganisationProfileIds());
                return paramValues;
            }).toArray(MapSqlParameterSource[]::new);
    }

    @Query(value = """
        select user_id, last_updated, user_last_updated, access_types_min_version, deleted,
               access_types, organisation_id,
               organisation_status, organisation_profile_ids, active, retry, retry_after
        from user_refresh_queue
        where active and retry < 4
        and (retry_after < now() or retry_after is null)
        limit 1
        for update skip locked""", nativeQuery = true)
    UserRefreshQueueEntity retrieveSingleActiveRecord();

    @Modifying
    @Query(value = """
        update user_refresh_queue
                              set active = false,
                              retry = 0,
                              retry_after = null
                              where user_id = :userId
                              and last_updated <= :lastUpdated
                              and access_types_min_version <= :accessTypesMinVersion""", nativeQuery = true)
    void clearUserRefreshRecord(String userId, LocalDateTime lastUpdated,
                                                  Long accessTypesMinVersion);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query(value = "update user_refresh_queue "
            + "set "
            + "retry = case "
            + "when retry = 0 then 1 "
            + "when retry = 1 then 2 "
            + "when retry = 2 then 3 "
            + "else 4 "
            + "end, "
            + "retry_after = case "
            + "when retry = 0 then now() + (interval '1' Minute) * :retryOneIntervalMin "
            + "when retry = 1 then now() + (interval '1' Minute) * :retryTwoIntervalMin "
            + "when retry = 2 then now() + (interval '1' Minute) * :retryThreeIntervalMin "
            + "else NULL "
            + "end "
            + "where user_id = :userId", nativeQuery = true)
    void updateRetry(String userId, String retryOneIntervalMin,
                     String retryTwoIntervalMin, String retryThreeIntervalMin);

    @Query(value = "select count(*) from user_refresh_queue where active = true and (retry_after < now()"
            + "or retry_after is null)",
            nativeQuery = true)
    Long getActiveUserRefreshQueueCount();

}
