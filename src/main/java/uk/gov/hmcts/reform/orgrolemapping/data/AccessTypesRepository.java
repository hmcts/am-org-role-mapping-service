package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTypesRepository extends JpaRepository<AccessTypesEntity, Long> {

    @Query(value = "SELECT version AS version, "
            + "CAST(access_types AS TEXT) AS access_types "
            + "FROM access_types FOR UPDATE",
            nativeQuery = true)
    AccessTypesEntity getAccessTypesEntity();

    @Query(value = "update access_types "
           + "set version = version + 1, access_types = CAST(:accessTypes AS jsonb) "
           + "returning version, CAST(access_types AS TEXT) AS access_types",
            nativeQuery = true)
    AccessTypesEntity updateAccessTypesEntity(String accessTypes);
}
