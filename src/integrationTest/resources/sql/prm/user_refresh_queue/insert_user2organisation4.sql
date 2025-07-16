INSERT INTO user_refresh_queue (user_id, organisation_id, access_types_min_version, active, organisation_profile_ids, organisation_status, last_updated, user_last_updated, access_types)
SELECT 'user2', '4', 2, true, '{SOLICITOR_PROFILE}', 'ACTIVE', '2020-01-01T13:20:01.046Z', '2020-01-01T13:30:01.046Z',
'{"jurisdictionId": "CIVIL", "organisationProfileId": "SOLICITOR_PROFILE", "accessTypeId": "civil-cases-1","enabled": true}';
