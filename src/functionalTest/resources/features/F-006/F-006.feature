@F-006
Feature: F-006 : Create Role Assignments for Org Staff Roles SSCS

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-051
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
  Scenario: must successfully create organisational role mapping for dwp and hmrc
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-053_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-053_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-053_DeleteDataForRoleAssignments].

