package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchLastRunTimestampRepository extends JpaRepository<BatchLastRunTimestampEntity, Long> {

}
