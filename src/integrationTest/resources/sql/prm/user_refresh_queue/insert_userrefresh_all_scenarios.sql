INSERT INTO user_refresh_queue (user_id, organisation_id, access_types_min_version, active, organisation_profile_ids, organisation_status, last_updated, user_last_updated, access_types)
SELECT 'USERX', 'ORG1', 2, true, '{ORGPROFILE2}', 'ACTIVE', '2020-01-01T13:20:01.046Z', '2020-01-01T13:30:01.046Z', '[
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yyy_1_2_2a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yyy_1__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yyy_1_2_2a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yyy_2__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yyn_3_4_4a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yyn_3__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yyn_3_4_4a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yyn_4__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yny_5_6_6a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yny_5__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_yny_5_6_6a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_yny_6__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_ynn__7_8_8a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_ynn_7__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_ynn__7_8_8a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_ynn_8__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nyy_9_10_10a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nyy_9__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nyy_9_10_10a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nyy_10__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nyn_11_12_12a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nyn_11__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nyn_11_12_12a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nyn_12__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nny_13_14_14a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nny_13__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nny_13_14_14a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nny_14__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    },


    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nnn__15_16_16a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nnn_15__UserAccessType_Enabled__ACCESSTYPE",
        "enabled": true
    },
    {
        "organisationProfileId": "ORGPROFILE2",
        "jurisdictionId": "SCENARIO_nnn__15_16_16a__JURISDICTION__ORGPROFILE2",
        "accessTypeId": "SCENARIO_nnn_16__UserAccessType_Disabled__ACCESSTYPE",
        "enabled": false
    }
]';
