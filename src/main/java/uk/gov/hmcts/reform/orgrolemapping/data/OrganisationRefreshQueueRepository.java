package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES_MIN_VERSION;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_ID;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_LAST_UPDATED;

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
                + "where excluded.organisation_last_updated > organisation_refresh_queue.organisation_last_updated";

        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationIdentifier());
            paramValues.addValue(ORGANISATION_LAST_UPDATED, r.getOrganisationLastUpdated());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }
}
