DELETE FROM profile_refresh_queue;

INSERT INTO profile_refresh_queue (organisation_profile_id, access_types_min_version, active)
SELECT unnest(array[string_to_array('SOLICITOR_ORG', ',')]), 1, true;

DELETE FROM organisation_refresh_queue;

INSERT INTO organisation_refresh_queue (organisation_id, organisation_last_updated, last_updated, access_types_min_version, active)
VALUES ('123', '2024-02-06 12:51:11.158', '2024-02-06 12:51:11.158', 1, true);
