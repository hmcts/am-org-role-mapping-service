package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTypesRepository extends JpaRepository<AccessTypesEntity, Long> {

    /*@Query(value = "SELECT version AS version, "
            + "CAST(access_types AS TEXT) AS access_types "
            + "FROM access_types FOR UPDATE;",
            nativeQuery = true)
    AccessTypesEntity getAccessTypesEntity();*/

    @Query(value = "SELECT nextval('access_types_version_seq')", nativeQuery =
            true)
    Long getNextAccessTypeVersion();

}
