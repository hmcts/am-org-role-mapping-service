package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTypesRepository extends CrudRepository<AccessTypes, Long> {

    String findByAccessTypeVersion(Long version);
}
