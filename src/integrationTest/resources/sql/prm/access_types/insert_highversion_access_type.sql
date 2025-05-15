DELETE FROM access_types;

INSERT INTO public.access_types (version, access_types)
VALUES (50, '{ "organisation_profiles": { "SOLICITOR_PROFILE": { "CIVIL": { "access_type_id": "CIVIL_SOLICITOR_0", "access_mandatory": false, "access_default": false, "display": true, "description": "Civil Solicitor 0 description", "roles": [ {"case_type_id": "civil_case_type_0", "organisational_role_name": "orgRole1", "group_role_name": "groupRole1", "case_group_id_template": "CIVIL:$ORGID$", "group_access_enabled": true } ] } } } }');
