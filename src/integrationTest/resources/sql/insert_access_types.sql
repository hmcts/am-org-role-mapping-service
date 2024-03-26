DELETE FROM access_types;

INSERT INTO public.access_types (version, access_types)
VALUES(0, '{}');
-- INSERT INTO access_types (version, access_types)
-- VALUES (1, '{ "organisation_profiles": { "SOLICITOR_ORG": { "CIVIL": { "access_type_id": "at1", "access_mandatory": false, "access_default": false, "roles": [], "case_type_id": "ctId", "organisational_role_name": "orn1", "group_role_name": "grn1", "case_group_id_template": "cgit1", "group_access_enabled": false } } } }');
