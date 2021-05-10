package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshJobsRepository extends CrudRepository<RefreshJobEntity, String> {

    @Query("select rj from refresh_jobs as rj where status=?1")
    List<RefreshJobEntity> findByRefreshJobStatus(String status);
}
