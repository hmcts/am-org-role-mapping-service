DELETE FROM access_types;
DELETE FROM user_refresh_queue;

INSERT INTO public.access_types (access_types, version)
VALUES('{ "organisationProfiles":
[{"organisationProfileId": "SOLICITOR_PROFILE",
    "jurisdictions":
    [{"jurisdictionId": "BEFTA_JURISDICTION_1",
        "accessTypes": [{"accessTypeId": "1","accessMandatory": true,"accessDefault": true,
            "roles": [{"caseTypeId": "23","organisationalRoleName": "organisationRoleName1","groupRoleName": "groupname1","caseGroupIdTemplate": "CIVIL:all:CIVIL:AS1:$ORGID$","groupAccessEnabled": true}
                     ]}]},
     {"jurisdictionId": "BEFTA_JURISDICTION_2",
                  "accessTypes": [{"accessTypeId": "2","accessMandatory": true,"accessDefault": true,
             "roles": [{"caseTypeId": "23","organisationalRoleName": "organisationRoleName2","groupRoleName": "groupname2","caseGroupIdTemplate": "IA:all:IA:AS1:$ORGID$","groupAccessEnabled": true}]}]}
                     ]}]}'::jsonb, 2);

INSERT INTO public.user_refresh_queue (user_id, last_updated, access_types_min_version, deleted, access_types, organisation_id, organisation_status, organisation_profile_ids, active)
VALUES(1, now(), 1, null, '[{ "jurisdictionId": "BEFTA_JURISDICTION_1","organisationProfileId": "SOLICITOR_PROFILE","accessTypeId": "1","enabled": true} ,{ "jurisdictionId": "BEFTA_JURISDICTION_2","organisationProfileId": "SOLICITOR_PROFILE","accessTypeId": "2","enabled": true}]'::jsonb, 'OrgId', 'OrgStatus', '{"SOLICITOR_PROFILE","2"}', true);



