DELETE FROM profile_refresh_queue;

INSERT INTO profile_refresh_queue (organisation_profile_id, access_types_min_version, active)
SELECT unnest(array[string_to_array('SOLICITOR_ORG', ',')]), 1, true;
INSERT INTO profile_refresh_queue (organisation_profile_id, access_types_min_version, active)
SELECT unnest(array[string_to_array('DWP_ORG', ',')]), 1, false;