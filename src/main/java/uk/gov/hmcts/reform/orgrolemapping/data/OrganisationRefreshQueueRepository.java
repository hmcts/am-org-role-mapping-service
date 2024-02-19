package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface OrganisationRefreshQueueRepository extends JpaRepository<OrganisationRefreshQueueEntity, String> {

    @Query(value = "select organisation_id, last_updated, access_types_min_version, active, retry "
            + "from organisation_refresh_queue "
            + "where active = true "
            + "limit 1 "
            + "for update skip locked", nativeQuery = true)
    OrganisationRefreshQueueEntity findAndLockSingleActiveOrganisationRecord();

    @Query(value = "select count(*) from organisation_refresh_queue where active = true", nativeQuery = true)
    Long getActiveOrganisationRefreshQueueCount();

    @Modifying
    @Query(value = "update organisation_refresh_queue "
            + "set active = false "
            + "where organisation_id = :organisationId "
            + "and access_types_min_version <= :accessTypeMinVersion "
            + "and last_updated <= :lastUpdated", nativeQuery = true)
    void setActiveFalse(String organisationId, Integer accessTypeMinVersion, LocalDateTime lastUpdated);

//    @Transactional
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query(value = "update organisation_refresh_queue "
            + "set retry = :retryVal "
            + "where organisation_id = :organisationId", nativeQuery = true)
    void setRetry(String organisationId, Integer retryVal);
}