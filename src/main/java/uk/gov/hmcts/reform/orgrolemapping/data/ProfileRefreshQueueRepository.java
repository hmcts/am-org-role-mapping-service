package uk.gov.hmcts.reform.orgrolemapping.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRefreshQueueRepository extends JpaRepository<ProfileRefreshQueueEntity, String> {

    @Query(value = "select organisation_profile_id, access_types_min_version, active "
            + "from profile_refresh_queue "
            + "where active "
            + "for update skip locked", nativeQuery = true)
    List<ProfileRefreshQueueEntity> getActiveProfileEntities();

    @Modifying
    @Query(value = "update profile_refresh_queue "
            + "set active = false "
            + "where organisation_profile_id = :organisationProfileId "
            + "and access_types_min_version <= :accessTypeMaxVersion", nativeQuery = true)
    void setActiveFalse(String organisationProfileId, Integer accessTypeMaxVersion);
}