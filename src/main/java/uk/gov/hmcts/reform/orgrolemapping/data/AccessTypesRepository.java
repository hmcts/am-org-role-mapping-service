package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTypesRepository extends CrudRepository<AccessTypes, Long> {

    //retrieves the AccessTypes record by version.
    AccessTypes findByVersion(Long version);

    // Retrieves the first AccessTypes record.
    // 1. It first sorts the AccessTypes records in descending order by version.
    // 2.Hence, we get the top version among all the AccessTypes
    AccessTypes findFirstByOrderByVersionDesc();

}
