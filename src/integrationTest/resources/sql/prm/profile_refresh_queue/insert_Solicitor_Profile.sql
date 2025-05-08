DELETE FROM profile_refresh_queue;

INSERT INTO profile_refresh_queue (organisation_profile_id, access_types_min_version, active)
SELECT unnest(array[string_to_array('SOLICITOR_PROFILE', ',')]), 1, true;
