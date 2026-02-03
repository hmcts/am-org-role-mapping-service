package uk.gov.hmcts.reform.orgrolemapping.data.irm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;

@Repository
public interface IdamRoleManagementQueueRepository extends JpaRepository<IdamRoleManagementQueueEntity, String> {

    @Modifying
    @Query(value = """
        insert into idam_role_management_queue (user_id, user_type, data, last_updated, active)
        values (:userId, :userType, :data, :lastUpdated, true)
        on conflict (user_id) do update
        set last_updated = now(),
            active = true,
            retry = 0,
            retry_after = now(),
            user_type = excluded.user_type,
            data = excluded.data
        where excluded.last_updated > idam_role_management_queue.last_updated
        """, nativeQuery = true)
    void upsert(String userId, String userType, String data, LocalDateTime lastUpdated);

    @Modifying
    @Query(value = """
        update idam_role_management_queue
        set active = false,
            retry = 0,
            retry_after = now()
        where user_id = :userId
        and last_updated <= :lastUpdated
        """, nativeQuery = true)
    void clearRecord(String userId, LocalDateTime lastUpdated);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query(value = """
        update idam_role_management_queue 
        set retry = case 
                when retry = 0 then 1 
                when retry = 1 then 2 
                when retry = 2 then 3 
                when retry = 4 then 0 
                else 4 
            end, 
            retry_after = case 
                when retry = 0 then now() + (interval '1' Minute) * CAST(:retryOneIntervalMin AS INTEGER) 
                when retry = 1 then now() + (interval '1' Minute) * CAST(:retryTwoIntervalMin AS INTEGER) 
                when retry = 2 then now() + (interval '1' Minute) * CAST(:retryThreeIntervalMin AS INTEGER) 
                when retry = 4 then now() 
                else NULL 
            end 
        where user_id = :userId
        """, nativeQuery = true)
    void updateRetry(String userId, 
                     String retryOneIntervalMin,
                     String retryTwoIntervalMin, 
                     String retryThreeIntervalMin);
}
