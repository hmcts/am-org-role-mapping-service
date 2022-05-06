@F-005
Feature: F-005 : Create Role Assignments for Hearing Roles

  Background:
    Given an appropriate test context as detailed in the test data source

#  @S-041
#  Scenario: must successfully create organisational role mapping for listed-hearing-viewer
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-041_DeleteDataForRoleAssignments]
#    And a successful call [to post create organisational role mapping request on ORM] as in [S-041_PostCreateRequestToORM],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-041_DeleteDataForRoleAssignments].

  @S-042
  Scenario: must successfully create organisational role mapping for hearing-viewer and hearing-manager
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-042_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-042_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-042_DeleteDataForRoleAssignments].

