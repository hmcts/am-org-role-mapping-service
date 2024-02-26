package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRefreshQueueRepository extends JpaRepository<UserRefreshQueueEntity, String> {

    @Query(value = "DELETE FROM user_refresh_queue o WHERE "
            + "o.last_updated > (now() - interval ':numDaysPassed day') "
            + "AND o.active", nativeQuery = true)
    void deleteActiveUserRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(
            @Param("numDaysPassed") String numDaysPassed);

}
