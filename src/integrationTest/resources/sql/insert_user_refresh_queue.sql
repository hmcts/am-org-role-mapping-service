DELETE FROM access_types;
DELETE FROM user_refresh_queue;

INSERT INTO public.access_types (access_types, version)
VALUES('{}'::jsonb, 2);

INSERT INTO public.user_refresh_queue (user_id, user_last_updated, last_updated, access_types_min_version, deleted, access_types, organisation_id, organisation_status, organisation_profile_ids, active)
VALUES(1, now(), now(), 1, null, '[]'::jsonb, 'OrgId', 'OrgStatus', '{"profileId"}', true);
INSERT INTO public.user_refresh_queue (user_id, user_last_updated, last_updated, access_types_min_version, deleted, access_types, organisation_id, organisation_status, organisation_profile_ids, active)
VALUES(2, now(), now(), 1, null, '[]'::jsonb, 'OrgId', 'OrgStatus', '{"profileId"}', false);
INSERT INTO public.user_refresh_queue (user_id, user_last_updated, last_updated, access_types_min_version, deleted, access_types, organisation_id, organisation_status, organisation_profile_ids, active)
VALUES(3, now(), now(), 1, null, '[]'::jsonb, 'OrgId', 'OrgStatus', '{"profileId"}', true);




