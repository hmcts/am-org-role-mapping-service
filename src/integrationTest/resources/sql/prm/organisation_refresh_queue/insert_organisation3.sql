INSERT INTO organisation_refresh_queue (organisation_id, access_types_min_version, active, organisation_last_updated, last_updated, retry, retry_after)
SELECT '3', 1, false, '2020-01-01T13:20:01.046Z', '2020-01-01T13:30:01.046Z', 0, '2020-01-01T13:20:01.046Z';
