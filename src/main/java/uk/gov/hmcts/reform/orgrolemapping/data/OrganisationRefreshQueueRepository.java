package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.OrganisationInfo;

import java.util.List;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ACCESS_TYPES_MIN_VERSION;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.LAST_UPDATED;
import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.ORGANISATION_ID;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    default void upsertToOrganisationRefreshQueue(NamedParameterJdbcTemplate jdbcTemplate,
                                                  List<OrganisationInfo> rows,
                                                  Integer accessTypeMinVersion) {
        String sql =
                "insert into organisation_refresh_queue "
                + "(organisation_id, last_updated, access_types_min_version, active) "
                + "values (:organisationId, :lastUpdated, :accessTypesMinVersion, true) "
                + "on conflict (organisation_id) do update "
                + "set "
                + "access_types_min_version = excluded.access_types_min_version, "
                + "last_updated = greatest(excluded.last_updated, organisation_refresh_queue.last_updated), "
                + "active = true "
                + "where excluded.access_types_min_version > organisation_refresh_queue.access_types_min_version";

        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationIdentifier());
            paramValues.addValue(LAST_UPDATED, r.getLastUpdated());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }
}
