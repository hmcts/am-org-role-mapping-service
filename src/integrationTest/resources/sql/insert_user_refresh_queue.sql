DELETE FROM user_refresh_queue;

INSERT INTO user_refresh_queue (user_id, user_last_updated, access_types_min_version, deleted, access_types,
organisation_id, organisation_status, organisation_profile_ids, active)
VALUES ('123', '2024-02-06 12:51:11.158', 1, NULL, '123', '123', 'ACTIVE', '{"SOLICITOR_PROFILE"}', true);