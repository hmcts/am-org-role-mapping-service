package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTypesRepository extends CrudRepository<AccessTypesEntity, Long> {

    Optional<AccessTypesEntity> findFirstByOrderByVersionDesc();
}
