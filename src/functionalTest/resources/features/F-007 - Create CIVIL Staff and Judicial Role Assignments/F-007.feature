@F-007
Feature: F-007 : Create Role Assignments for CIVIL Caseworker and Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-061
  @FeatureToggle(DB:civil_wa_1_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-061_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Business Centre Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments].

  @S-062
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader and Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-062_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-062_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-062_DeleteDataForRoleAssignments].

  @S-063
  @FeatureToggle(DB:civil_wa_1_4=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Tribunal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-063_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-063_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Tribunal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-063_DeleteDataForRoleAssignments].

  @S-064
  @FeatureToggle(DB:civil_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-061_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments].

  @S-081
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Judge
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-081_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-081_PushMessageToJRDService],
    And the request [contains the actorId of the user just published who has judge role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-081_DeleteDataForRoleAssignments].

  @S-082
  @FeatureToggle(DB:civil_wa_1_9=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for circuit Judge
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-082_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-082_PushMessageToJRDService],
    And the request [contains the actorId of the user just published who has circuit judge role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-082_DeleteDataForRoleAssignments].

  @S-084
  @FeatureToggle(DB:civil_wa_2_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for fee paid Judge
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-084_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-084_PushMessageToJRDService],
    And the request [contains the actorId of the user just published who has fee paid judge role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-084_DeleteDataForRoleAssignments].

  @S-007.01_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker (AAA6 Civil)] as in [S-007.01_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.01_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker (AAA7 Civil)] as in [S-007.01_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.01a_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Task Supervisor (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Task Supervisor (AAA6 Civil)] as in [S-007.01a_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Task Supervisor (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.01a_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Task Supervisor (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Task Supervisor (AAA7 Civil)] as in [S-007.01a_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Task Supervisor (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.01b_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Case allocator (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Case allocator (AAA6 Civil)] as in [S-007.01b_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.01b_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Case allocator (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Case allocator (AAA7 Civil)] as in [S-007.01b_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (AAA6 Civil)] as in [S-007.03_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (AAA7 Civil)] as in [S-007.03_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03a_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (AAA6 Civil)] as in [S-007.03a_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03a_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (AAA7 Civil)] as in [S-007.03a_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03b_AAA6
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case allocator (AAA6)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case allocator (AAA6 Civil)] as in [S-007.03b_AAA6__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role (AAA6)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-007.03b_AAA7
  @FeatureToggle(DB:civil_wa_2_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case allocator (AAA7)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case allocator (AAA7 Civil)] as in [S-007.03b_AAA7__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role (AAA7)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
