package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserInfo;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.domain.model.constants.PrmConstants.*;

public interface UserRefreshQueueRepository extends JpaRepository<UserRefreshQueueEntity, String> {
    default void insertIntoUserRefreshQueueForLastUpdated(NamedParameterJdbcTemplate jdbcTemplate,
                                                           List<UserInfo> rows,
                                                           Integer accessTypeMinVersion) {
        String sql =
                "insert into user_refresh_queue (user_id, last_updated, access_types_min_version, deleted,"
                    + "access_types, organisation_id, organisation_status, organisation_profile_ids, active)"
                        //TODO: CHECK ABOUT THE JSON "access_types" - I think this is wrong
                    + "values (:userId, :lastUpdated, :accessTypesMinVersion, :deleted, "
                        +":accessTypes, :organisationId, :organisationStatus, :organisationProfileIds)"
                    + "on conflict (organisation_id) do update "
                    + "set access_types_min_version = greatest(excluded.access_types_min_version, "
                    + "organisation_refresh_queue.access_types_min_version), last_updated = excluded.last_updated, "
                    + "active = true"
                    + "where excluded.last_updated > organisation_refresh_queue.last_updated";

        //TODO: Check about DELETED and ACTIVE fields. (ACTIVE currently not included)
        MapSqlParameterSource[] params = rows.stream().map(r -> {
            MapSqlParameterSource paramValues = new MapSqlParameterSource();
            paramValues.addValue(USER_ID, r.getUserId());
            paramValues.addValue(LAST_UPDATED, r.getLastUpdated());
            paramValues.addValue(DELETED, r.getDeleted());
            paramValues.addValue(ORGANISATION_ID, r.getOrganisationId());
            paramValues.addValue(ORGANISATION_STATUS, r.getOrganisationStatus());
            paramValues.addValue(ORGANISATION_PROFILE_IDS, r.getOrganisationProfileIds());
            paramValues.addValue(ACCESS_TYPES, r.getAccessTypes());
            paramValues.addValue(ACCESS_TYPES_MIN_VERSION, accessTypeMinVersion);
            return paramValues;
        }).toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, params);
    }
}