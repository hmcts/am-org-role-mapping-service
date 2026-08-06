package uk.gov.hmcts.reform.orgrolemapping.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDateTimeRepository extends JpaRepository<DatabaseDateTime, Long> {

    @Query(value = "SELECT CURRENT_TIMESTAMP AS DATE_VALUE", nativeQuery = true)
    DatabaseDateTime getCurrentTimeStamp();

}
