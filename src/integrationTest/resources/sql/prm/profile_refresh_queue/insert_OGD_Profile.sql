INSERT INTO profile_refresh_queue (organisation_profile_id, access_types_min_version, active)
SELECT unnest(array[string_to_array('OGD_PROFILE', ',')]), 2, true;
