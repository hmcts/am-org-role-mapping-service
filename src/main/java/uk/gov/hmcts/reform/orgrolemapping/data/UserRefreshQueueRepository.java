package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.ProfessionalUserData;

import java.util.List;

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

    default void upsertToUserRefreshQueue(NamedParameterJdbcTemplate jdbcTemplate,
                                          List<ProfessionalUserData> rows,
                                          Integer accessTypeMinVersion) {
        String sql =
            "insert into user_refresh_queue (user_id, user_last_updated, access_types_min_version, " +
            "deleted, access_types, "
            + "organisation_id, organisation_status, organisation_profile_ids, active) "
            + "values(:userId, :userLastUpdated, :accessTypesMinVersion, :deleted, CAST(:accessTypes AS" +
            " jsonb), "
            + ":organisationId, :organisationStatus, string_to_array(:organisationProfileIds, ','), true) "
            + "on conflict (user_id) do update "
            + "set "
            + "access_types_min_version = greatest(excluded.access_types_min_version, "
                                                + "user_refresh_queue.access_types_min_version), "
            + "user_last_updated = greatest(excluded.user_last_updated, user_refresh_queue.user_last_updated), "
            + "last_updated = now(), "
            + "active = true, "
            + "deleted = case "
                + "when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.deleted "
                + "else user_refresh_queue.deleted end, "
            + "access_types = case "
                + "when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.access_types "
                + "else user_refresh_queue.access_types end, "
            + "organisation_id = case "
                + "when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.organisation_id "
                + "else user_refresh_queue.organisation_id end, "
            + "organisation_status = case "
                + "when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.organisation_status "
                + "else user_refresh_queue.organisation_status end, "
            + "organisation_profile_ids = case "
                + "when excluded.user_last_updated > user_refresh_queue.user_last_updated then excluded.organisation_profile_ids "
                + "else user_refresh_queue.organisation_profile_ids end";

        MapSqlParameterSource[] params = rows.stream().map(r -> {
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

        jdbcTemplate.batchUpdate(sql, params);
    }
}
