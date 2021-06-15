package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlagConfigRepository extends CrudRepository<FlagConfig, Long> {

    FlagConfig findByFlagNameAndEnv(String flagName, String envName);
}
