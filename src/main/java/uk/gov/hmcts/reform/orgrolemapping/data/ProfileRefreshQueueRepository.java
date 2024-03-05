package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRefreshQueueRepository extends JpaRepository<ProfileRefreshQueueEntity, String> {

    @Modifying
    @Query(value = "insert into profile_refresh_queue (organisation_profile_id, access_types_min_version, active) "
            + "select unnest(array[string_to_array(:organisationProfileIds, ',')]), :accessTypesMinVersion, true "
            + "on conflict (organisation_profile_id) do update "
            + "set "
            + "access_types_min_version = excluded.access_types_min_version, "
            + "active = true "
            + "where excluded.access_types_min_version > profile_refresh_queue.access_types_min_version",
            nativeQuery = true)
    void upsertOrganisationProfileIds(
            @Param("organisationProfileIds") String organisationProfileIds,
            @Param("accessTypesMinVersion") Long accessTypesMinVersion
    );
}
