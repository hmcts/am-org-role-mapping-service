DELETE FROM access_types;

INSERT INTO public.access_types (version, access_types)
VALUES(1, '{"organisationProfiles": [{"jurisdictions": [{"accessTypes": [{"roles": [{"caseTypeId": "CIVIL_Case_TYPE",
"groupRoleName": "CIVIL_Group_Role2", "groupAccessEnabled": false, "caseGroupIdTemplate": "CIVIL_CaseType:[GrpRoleName1]:$ORGID$",
"organisationalRoleName": "CIVIL_Org_Role1"}], "accessTypeId": "CIVIL_ACCESS_TYPE_ID", "accessDefault": true, "accessMandatory": true}],
 "jurisdictionId": "CIVIL"}], "organisationProfileId": "CIVIL_SOLICITOR_PROFILE"}]}
');


