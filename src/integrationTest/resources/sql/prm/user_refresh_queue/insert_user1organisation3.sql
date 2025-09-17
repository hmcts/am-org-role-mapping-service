INSERT INTO user_refresh_queue (user_id, organisation_id, access_types_min_version, active, organisation_profile_ids, organisation_status, last_updated, user_last_updated)
SELECT 'user1', '3', 1, true, '{SOLICITOR_PROFILE}', 'BLOCKED', '2020-01-01T13:20:01.046Z', '2020-01-01T13:30:01.046Z';
