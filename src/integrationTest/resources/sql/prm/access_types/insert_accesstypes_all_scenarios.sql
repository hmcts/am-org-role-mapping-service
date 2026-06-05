DELETE FROM access_types;

-- accessDefault=false, accessMandatory=false, groupAccessEnabled=false
INSERT INTO public.access_types (version, access_types)
VALUES (50, '{
    "organisationProfiles": [
        {
            "organisationProfileId": "ORGPROFILE2",
            "jurisdictions": [
                {
                    "jurisdictionId": "SCENARIO_yyy_1_2_2a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yyy_1__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyy_2__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyy_2a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_yyn_3_4_4a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yyn_3__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyn_4__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyn_4a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_yny_5_6_6a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yny_5__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yny_6__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yny_6a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_ynn__7_8_8a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_ynn_7__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_ynn_8__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_ynn_8a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nyy_9_10_10a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nyy_9__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyy_10__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyy_10a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nyn_11_12_12a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nyn_11__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyn_12__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyn_12a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nny_13_14_14a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nny_13__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nny_14__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nny_14a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nnn__15_16_16a__JURISDICTION__ORGPROFILE2",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nnn_15__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nnn_16__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nnn_16a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                }
            ]
        },


        {
            "organisationProfileId": "ORGPROFILE3_IGNORED",
            "jurisdictions": [
                {
                    "jurisdictionId": "SCENARIO_yyy_1_2_2a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yyy_1__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_1__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_1__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyy_2__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_2__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyy_2a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyy_2a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yyy_2a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_yyn_3_4_4a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yyn_3__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_3__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_3__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyn_4__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_4__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yyn_4a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yyn_4a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_yyn_4a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_yny_5_6_6a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_yny_5__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_5__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_5__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yny_6__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_6__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_yny_6a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_yny_6a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_yny_6a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_ynn__7_8_8a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_ynn_7__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_7__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_7__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_ynn_8__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_8__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_ynn_8a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": true,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_ynn_8a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_ynn_8a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nyy_9_10_10a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nyy_9__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_9__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_9__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyy_10__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_10__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyy_10a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyy_10a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nyy_10a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nyn_11_12_12a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nyn_11__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_11__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_11__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyn_12__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_12__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nyn_12a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": true,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nyn_12a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nyn_12a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nny_13_14_14a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nny_13__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_13__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_13__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nny_14__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_14__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nny_14a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "groupRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nny_14a__CASETYPE",
                                    "groupAccessEnabled": true,
                                    "organisationalRoleName": "SCENARIO_nny_14a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                },


                {
                    "jurisdictionId": "SCENARIO_nnn__15_16_16a__JURISDICTION__ORGPROFILE3_IGNORED",
                    "accessTypes": [
                        {
                            "accessTypeId": "SCENARIO_nnn_15__UserAccessType_Enabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_15__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_15__UserAccessType_Enabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nnn_16__UserAccessType_Disabled__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_16__UserAccessType_Disabled__OrgRole"
                                }
                            ]
                        },
                        {
                            "accessTypeId": "SCENARIO_nnn_16a__UserAccessType_Missing__ACCESSTYPE",
                            "accessDefault": false,
                            "accessMandatory": false,
                            "roles": [
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__GroupRole",
                                    "caseGroupIdTemplate": "BEFTA_MASTER:$ORGID$",
                                    "organisationalRoleName": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "groupRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__GroupRole__MissingTemplate",
                                    "caseGroupIdTemplate": ""
                                },
                                {
                                    "caseTypeId": "SCENARIO_nnn_16a__CASETYPE",
                                    "groupAccessEnabled": false,
                                    "organisationalRoleName": "SCENARIO_nnn_16a__UserAccessType_Missing__OrgRole"
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
}');
