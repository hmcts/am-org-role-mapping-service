package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES_MIN_VERSION;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_LAST_UPDATED;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    // NB: This upsert is for PRM Process 2:
    default void upsertToOrganisationRefreshQueue(NamedParameterJdbcTemplate jdbcTemplate,
                                                  List<OrganisationInfo> rows,
                                                  Integer accessTypeMinVersion) {
        String sql = """
                insert into organisation_refresh_queue 
                (organisation_id, organisation_last_updated, access_types_min_version, active) 
                values (:organisationId, :organisationLastUpdated, :accessTypesMinVersion, true) 
                on conflict (organisation_id) do update 
                set 
                access_types_min_version = excluded.access_types_min_version, 
                organisation_last_updated = greatest(excluded.organisation_last_updated, 
                organisation_refresh_queue.organisation_last_updated), 
                last_updated = now(), 
                retry = 0,
                retry_after = now(),
                active = true 
                where excluded.access_types_min_version > organisation_refresh_queue.access_types_min_version
                """;

        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationIdentifier());
            paramValues.addValue(ORGANISATION_LAST_UPDATED, r.getOrganisationLastUpdated());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }


    // NB: This upsert is for PRM Process 3:
    default void upsertToOrganisationRefreshQueueForLastUpdated(
            NamedParameterJdbcTemplate jdbcTemplate,
            List<OrganisationInfo> rows,
            Integer accessTypeMinVersion) {
        String sql = """
                    insert into organisation_refresh_queue 
                    (organisation_id, organisation_last_updated, access_types_min_version, active) 
                    values (:organisationId, :organisationLastUpdated, :accessTypesMinVersion, true) 
                    on conflict (organisation_id) do update 
                    set 
                    access_types_min_version = greatest(excluded.access_types_min_version, 
                    organisation_refresh_queue.access_types_min_version), 
                    organisation_last_updated = excluded.organisation_last_updated, 
                    last_updated = now(), 
                    retry = 0,
                    retry_after = now(),
                    active = true 
                    where excluded.organisation_last_updated > organisation_refresh_queue.organisation_last_updated
                    """;

        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationIdentifier());
            paramValues.addValue(ORGANISATION_LAST_UPDATED, r.getOrganisationLastUpdated());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }

    // TODO: confirm if this implementation is needed
    default void insertIntoOrganisationRefreshQueueForLastUpdated(NamedParameterJdbcTemplate jdbcTemplate,
                                                                  List<OrganisationInfo> rows,
                                                                  Integer accessTypeMinVersion) {
        String sql =
                "insert into organisation_refresh_queue (organisation_id, organisation_last_updated, "
                + "access_types_min_version, active) "
                + "values (:organisationId, :organisationLastUpdated, :accessTypesMinVersion, true) "
                + "on conflict (organisation_id) do update "
                + "set access_types_min_version = greatest(excluded.access_types_min_version, "
                + "organisation_refresh_queue.access_types_min_version), "
                + "organisation_last_updated = excluded.organisation_last_updated, "
                + "last_updated = now(), "
                + "active = true "
                + "where excluded.organisation_last_updated "
                + "> organisation_refresh_queue.organisation_last_updated";

        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationIdentifier());
            paramValues.addValue(ORGANISATION_LAST_UPDATED, r.getOrganisationLastUpdated());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }

    @Query(value = "select organisation_id, organisation_last_updated, last_updated, access_types_min_version, "
                   + "active, retry, retry_after "
                   + "from organisation_refresh_queue "
                   + "where active = true "
                   + "and retry <= 4 "
                   + "and (retry_after < now() or retry_after is null)"
                   + "limit 1 "
                   + "for update skip locked", nativeQuery = true)
    OrganisationRefreshQueueEntity findAndLockSingleActiveOrganisationRecord();

    @Modifying
    @Query(value = """
          update organisation_refresh_queue 
                      set active = false ,
                      retry = 0,
                      retry_after = now()
                      where organisation_id = :organisationId 
                      and access_types_min_version <= :accessTypeMinVersion 
                      and last_updated <= :lastUpdated""", nativeQuery = true)
    void clearOrganisationRefreshRecord(String organisationId, Integer accessTypeMinVersion, LocalDateTime lastUpdated);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query(value = "update organisation_refresh_queue "
                   + "set "
                   + "retry = case "
                   + "when retry = 0 then 1 "
                   + "when retry = 1 then 2 "
                   + "when retry = 2 then 3 "
                   + "when retry = 4 then 0 "
                   + "else 4 "
                   + "end, "
                   + "retry_after = case "
                   + "when retry = 0 then now() + (interval '1' Minute) * CAST(:retryOneIntervalMin AS INTEGER) "
                   + "when retry = 1 then now() + (interval '1' Minute) * CAST(:retryTwoIntervalMin AS INTEGER) "
                   + "when retry = 2 then now() + (interval '1' Minute) * CAST(:retryThreeIntervalMin AS INTEGER) "
                   + "when retry = 4 then now() "
                   + "else NULL "
                   + "end "
                   + "where organisation_id = :organisationId", nativeQuery = true)
    void updateRetry(String organisationId, String retryOneIntervalMin,
                     String retryTwoIntervalMin, String retryThreeIntervalMin);

    @Modifying
    @Query(value = """
            DELETE FROM organisation_refresh_queue o 
            WHERE o.last_updated < 
                  (now() - ((interval '1' day) * CAST(:numDaysPassed AS INTEGER))) 
              AND o.active = false
              """, nativeQuery = true)
    int deleteInactiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(
            @Param("numDaysPassed") String numDaysPassed);
}
