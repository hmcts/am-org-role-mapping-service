package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshJobsRepository extends CrudRepository<RefreshJobEntity, Long> {

}
