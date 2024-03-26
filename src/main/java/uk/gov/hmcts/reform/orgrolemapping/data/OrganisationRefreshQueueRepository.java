package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES_MIN_VERSION;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_LAST_UPDATED;

import java.time.LocalDateTime;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    default void upsertToOrganisationRefreshQueue(NamedParameterJdbcTemplate jdbcTemplate,
                                                  List<OrganisationInfo> rows,
                                                  Integer accessTypeMinVersion) {
        String sql =
                "insert into organisation_refresh_queue "
                + "(organisation_id, organisation_last_updated, access_types_min_version, active) "
                + "values (:organisationId, :organisationLastUpdated, :accessTypesMinVersion, true) "
                + "on conflict (organisation_id) do update "
                + "set "
                + "access_types_min_version = excluded.access_types_min_version, "
                + "organisation_last_updated = greatest(excluded.organisation_last_updated, "
                + "organisation_refresh_queue.organisation_last_updated), "
                + "last_updated = now(), "
                + "active = true "
                + "where excluded.access_types_min_version > organisation_refresh_queue.access_types_min_version";

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
                   + "limit 1 "
                   + "for update skip locked", nativeQuery = true)
    OrganisationRefreshQueueEntity findAndLockSingleActiveOrganisationRecord();

    @Query(value = "select count(*) from organisation_refresh_queue where active = true and retry_after < now()",
            nativeQuery = true)
    Long getActiveOrganisationRefreshQueueCount();

    @Modifying
    @Query(value = "update organisation_refresh_queue "
                   + "set active = false "
                   + "where organisation_id = :organisationId "
                   + "and access_types_min_version <= :accessTypeMinVersion "
                   + "and last_updated <= :lastUpdated", nativeQuery = true)
    void setActiveFalse(String organisationId, Integer accessTypeMinVersion, LocalDateTime lastUpdated);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query(value = "update organisation_refresh_queue "
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
                   + "where organisation_id = :organisationId", nativeQuery = true)
    void updateRetry(String organisationId, String retryOneIntervalMin,
                     String retryTwoIntervalMin, String retryThreeIntervalMin);
}
