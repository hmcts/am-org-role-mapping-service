package uk.gov.hmcts.reform.orgrolemapping.data;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTypesRepository extends CrudRepository<AccessTypesEntity, Long> {

    Optional<AccessTypesEntity> findFirstByOrderByVersionDesc();

    @Query(value = "SELECT version AS version, "
            + "CAST(access_types AS TEXT) AS access_types "
            + "FROM access_types FOR UPDATE",
            nativeQuery = true)
    AccessTypesEntity getAccessTypesEntity();
}
