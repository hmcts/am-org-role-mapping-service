package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    @Modifying
    @Query(value = "DELETE FROM organisation_refresh_queue o WHERE "
            + "o.last_updated < now() - ((interval '1' day) * :numDaysPassed) "
            + "AND o.active = false", nativeQuery = true)

    void deleteActiveOrganisationRefreshQueueEntitiesLastUpdatedBeforeNumberOfDays(
            @Param("numDaysPassed") String numDaysPassed);
}
