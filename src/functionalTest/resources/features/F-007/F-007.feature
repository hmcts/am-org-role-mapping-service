@F-007
Feature: F-007 :Create Role Assignments for CIVIL Caseworker Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-061
#  Need to update for CIVIL tag
#  @FeatureToggle(IAC:iac_1_0=off) @FeatureToggle(IAC:iac_1_1=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader and CTSC Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-061_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Business Centre Team Leader role],
    And the request [contains the actorId of the user just published who has CTSC Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-061_DeleteDataForRoleAssignments].

  @S-062
#  Need to update for CIVIL tag
#  @FeatureToggle(IAC:iac_1_0=off) @FeatureToggle(IAC:iac_1_1=on)
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
