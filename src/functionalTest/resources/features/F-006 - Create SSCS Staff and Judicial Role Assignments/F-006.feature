@F-006
Feature: F-006 : Create Role Assignments for SSCS Staff and Judicial Org Roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-051
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on) @Ignore
  Scenario: must successfully create organisational role mapping for tribunal-caseworker and registrar
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-051_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-051_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-051_DeleteDataForRoleAssignments].

  @S-052
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for super-user and clerk
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-052_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-052_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-052_DeleteDataForRoleAssignments].

  @S-053
  @FeatureToggle(DB:sscs_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for dwp and hmrc
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-053_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-053_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-053_DeleteDataForRoleAssignments].

  @S-054
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-054_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-054_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-054_DeleteDataForRoleAssignments].

  @S-055
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader and Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-055_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-055_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-055_DeleteDataForRoleAssignments].

  @S-056
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Regional Centre Team Leader and Regional Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-056_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-056_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Regional Centre Team Leader role],
    And the request [contains the actorId of the user just published who has Regional Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-056_DeleteDataForRoleAssignments].

  @S-057
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader and CTSC Admin
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-057_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-057_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role],
    And the request [contains the actorId of the user just published who has CTSC Admin],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-057_DeleteDataForRoleAssignments].

  @S-058
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Tribunal Judge - Salaried appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-058_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-058_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorId of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-058_DeleteDataForRoleAssignments].

  @S-059
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Tribunal Judge - Fee Paid appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-059_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-059_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorId of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-059_DeleteDataForRoleAssignments].

   @S-060
   @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
   Scenario: must successfully create judicial role mapping for Tribunal Member Disability - Fee Paid appointment
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-060_DeleteDataForRoleAssignments],
     And a successful call [to publish existing JRD user ids to endpoint] as in [S-060_PushMessageToJRDService],
     And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
     And the request [contains the actorId of the user just published],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-060_DeleteDataForRoleAssignments].

  @S-161
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for President of Tribunal - Salaried appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-161_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-161_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorId of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-161_DeleteDataForRoleAssignments].

  @S-162
  @FeatureToggle(DB:sscs_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for ibca
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-162_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-162_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-162_DeleteDataForRoleAssignments].

