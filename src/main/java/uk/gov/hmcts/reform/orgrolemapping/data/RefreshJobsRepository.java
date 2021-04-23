package uk.gov.hmcts.reform.orgrolemapping.data;


import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface RefreshJobsRepository extends CrudRepository<RefreshJob, String> {
}
