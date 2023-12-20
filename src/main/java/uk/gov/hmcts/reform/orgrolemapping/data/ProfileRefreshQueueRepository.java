package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRefreshQueueRepository extends CrudRepository<ProfileRefreshQueue, String> {

    ProfileRefreshQueue findByOrganisationProfileId(String profileId);

    ProfileRefreshQueue findByOrganisationProfileIdAndAccessTypesMinVersion(String profileId, Long accessTypesMinVersion);

}
