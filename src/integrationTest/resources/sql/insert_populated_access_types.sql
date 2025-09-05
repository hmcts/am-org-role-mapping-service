DELETE FROM access_types;

INSERT INTO public.access_types (version, access_types)
VALUES(1, '{"organisationProfiles": [{"jurisdictions": [{"accessTypes": [{"roles": [{"caseTypeId": "CIVIL", "groupRoleName": "[APPLICANTSOLICITORONE]",
"groupAccessEnabled": true, "caseGroupIdTemplate": "CIVIL:all-cases:APPSOL1:$ORGID$", "organisationalRoleName": "Role1"}],
"accessTypeId": "civil-cases-1", "accessDefault": false, "accessMandatory": false}], "jurisdictionId": "CIVIL"}], "organisationProfileId": "SOLICITOR_ORG"}]}');