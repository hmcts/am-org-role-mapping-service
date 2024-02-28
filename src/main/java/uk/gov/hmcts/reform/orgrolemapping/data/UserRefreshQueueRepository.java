package uk.gov.hmcts.reform.orgrolemapping.data;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRefreshQueueRepository extends CrudRepository<UserRefreshQueueEntity, Long> {

    String SKIP_LOCKED = "-2";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserRefreshQueueEntity findByUserId(String userId);

    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserRefreshQueueEntity> findFirstByActiveTrue();

    @Modifying
    @Query(value = """
        insert into user_refresh_queue (user_id, user_last_updated, access_types_min_version, deleted,
                                        access_types, organisation_id, organisation_status,
                                        organisation_profile_ids, active)
        values(:userId, :userLastUpdated, :accessTypesMinVersion, :deleted, cast(:accessTypes as json),
         :organisationId, :organisationStatus, string_to_array(:organisationProfileIds, ','), true)
        on conflict (user_id) do update
        set
            access_types_min_version = greatest(excluded.access_types_min_version,
             user_refresh_queue.access_types_min_version),
            user_last_updated = excluded.user_last_updated,
            last_updated = now(),
            deleted = excluded.deleted,
            access_types = excluded.access_types,
            organisation_id = excluded.organisation_id,
            organisation_status = excluded.organisation_status,
            organisation_profile_ids = excluded.organisation_profile_ids,
            active = true
        where excluded.user_last_updated > user_refresh_queue.user_last_updated
        """, nativeQuery = true)
    void upsert(String userId, LocalDateTime userLastUpdated, Long accessTypesMinVersion, LocalDateTime deleted,
                String accessTypes, String organisationId, String organisationStatus,
                String organisationProfileIds);
}
