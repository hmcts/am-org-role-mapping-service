@F-006
Feature: F-006 : Create Role Assignments for Org Staff Roles SSCS

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-051
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
  @FeatureToggle(DB:sscs_wa_1_0=on)
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
