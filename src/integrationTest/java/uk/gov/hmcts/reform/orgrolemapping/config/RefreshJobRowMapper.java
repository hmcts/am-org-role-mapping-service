package uk.gov.hmcts.reform.orgrolemapping.config;

import org.springframework.jdbc.core.RowMapper;
import uk.gov.hmcts.reform.orgrolemapping.data.RefreshJobEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RefreshJobRowMapper implements RowMapper<RefreshJobEntity> {
    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call {@code next()} on
     * the ResultSet; it is only supposed to map values of the current row.
     *
     * @param rs     the ResultSet to map (pre-initialized for the current row)
     * @param rowNum the number of the current row
     * @return the result object for the current row (may be {@code null})
     * @throws SQLException if an SQLException is encountered getting
     *                      column values (that is, there's no need to catch SQLException)
     */
    @Override
    public RefreshJobEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        RefreshJobEntity entity = new RefreshJobEntity();
        entity.setJobId(rs.getLong("job_id"));
        entity.setStatus(rs.getString("status"));
        return entity;
    }
}
