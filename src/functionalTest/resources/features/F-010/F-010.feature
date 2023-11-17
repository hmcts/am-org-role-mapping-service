@F-010
Feature: F-010 : Create Role Assignments for EmploymentTribunal Caseworker and Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-101
  @FeatureToggle(DB:employment_wa_1_0=on)
  Scenario: must successfully create org role mapping for Legal Caseworker and Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-101_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-101_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-101_DeleteDataForRoleAssignments].
