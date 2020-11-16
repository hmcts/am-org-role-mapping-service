@F-001
Feature: Create Role Assignments for Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-001 @FeatureToggle(orm-base-flag)
  Scenario: must successfully create org role mapping for multiple users with multiple role assignments
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains the data received from CRD API got transformed],
    And the request [contains multiple users with multiple role assignments],
    And it is submitted to call the [Create Role Assignments] operation of [Organisation Role Mapping Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-002
  Scenario: must successfully generate duplicate records message
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Create Role Assignments] operation of [Organisation Role Mapping Service],
    And the request [prepared to submit second time with no changes],
    And it is submitted to call the [Create Role Assignments] operation of [Organisation Role Mapping Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].