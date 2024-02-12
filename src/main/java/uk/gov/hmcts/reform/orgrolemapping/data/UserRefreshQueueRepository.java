package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserRefreshQueueRepository extends JpaRepository<UserRefreshQueueEntity, String> {

    @Modifying
    @Query(value = """
        insert into user_refresh_queue  (user_id, last_updated, access_types_min_version, deleted, access_types,
        organisation_id, organisation_status, organisation_profile_ids, active)
        values(:userId, :lastUpdated, :accessTypesMinVersion, :deleted, CAST(:accessTypes AS jsonb),
        :organisationId, :organisationStatus, string_to_array(:organisationProfileIds, ','), true)
        on conflict (user_id) do update
        set
        	access_types_min_version = greatest(excluded.access_types_min_version, user_refresh_queue.access_types_min_version),
        	last_updated = greatest(excluded.last_updated, user_refresh_queue.last_updated),
        	active = true,
        	deleted = case
        		when excluded.last_updated > user_refresh_queue.last_updated then excluded.deleted
                else user_refresh_queue.deleted end,
        	access_types = case
        		when excluded.last_updated > user_refresh_queue.last_updated then excluded.access_types
        		else user_refresh_queue.access_types end,
        	organisation_id = case
        		when excluded.last_updated > user_refresh_queue.last_updated then excluded.organisation_id
        		else user_refresh_queue.organisation_id end,
        	organisation_status = case
        		when excluded.last_updated > user_refresh_queue.last_updated then excluded.organisation_status
        		else user_refresh_queue.organisation_status end,
            organisation_profile_ids = case
        	    when excluded.last_updated > user_refresh_queue.last_updated then excluded.organisation_profile_ids
        		else user_refresh_queue.organisation_profile_ids end;
    """, nativeQuery = true)
    void upsertToUserRefreshQueue(
            String userId,
            LocalDateTime lastUpdated,
            Integer accessTypesMinVersion,
            LocalDateTime deleted,
            String accessTypes,
            String organisationId,
            String organisationStatus,
            String organisationProfileIds
    );
}
