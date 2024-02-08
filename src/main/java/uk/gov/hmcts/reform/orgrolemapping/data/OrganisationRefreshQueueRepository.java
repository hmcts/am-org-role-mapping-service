package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    @Modifying
    @Query(value =
            "insert into organisation_refresh_queue (organisation_id, last_updated, access_types_min_version, active) "
            + "values (:organisationId, :lastUpdated, :accessTypesMinVersion, true) "
            + "on conflict (organisation_id) do update "
            + "set "
            + "access_types_min_version = excluded.access_types_min_version, "
            + "last_updated = greatest(excluded.last_updated, organisation_refresh_queue.last_updated), "
            + "active = true "
            + "where excluded.access_types_min_version > organisation_refresh_queue.access_types_min_version",
            nativeQuery = true)
    void insertIntoOrganisationRefreshQueue(
            @Param("organisationId") String organisationId,
            @Param("lastUpdated") LocalDateTime lastUpdated,
            @Param("accessTypesMinVersion") Integer accessTypesMinVersion
    );

    @Modifying
    @Query(value =
            "insert into organisation_refresh_queue (organisation_id, last_updated, access_types_min_version, active) "
                    + "values (:organisationId, :lastUpdated, :accessTypesMinVersion, true) "
                    + "on conflict (organisation_id) do update "
                    + "set access_types_min_version = greatest(excluded.access_types_min_version, "
                    + "organisation_refresh_queue.access_types_min_version), last_updated = excluded.last_updated, "
                    + "active = true "
                    + "where excluded.last_updated > organisation_refresh_queue.last_updated",
            nativeQuery = true)
    void insertIntoOrganisationRefreshQueueForLastUpdated(
            @Param("organisationId") String organisationId,
            @Param("lastUpdated") LocalDateTime lastUpdated,
            @Param("accessTypesMinVersion") Integer accessTypesMinVersion
    );
}
