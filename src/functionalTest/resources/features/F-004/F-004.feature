@F-004
Feature: F-004 : Refresh Role Assignments for Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-031 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role assignments for a user having single judicial booking
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-031_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [S-031_DeleteDataForBookings],
    And a successful call [to create single booking for this user] as in [S-031_CreateDataForBookings],
    And a successful call [to post judicial assignments refresh request on ORM] as in [S-031_PostRefreshRequestToORM],
    And the request [contains the actorIds of the user having existing judicial role assignments],
    And the request [contains the actorIds of the user for which above booking is created],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-031_DeleteDataForRoleAssignments]
  @S-032 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mappings for User having multiple Judicial bookings
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-032_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [S-032_DeleteDataForBookings],
    And a successful call [to create single booking for this user] as in [S-032_CreateDataForBookings],
    And a successful call [to create single booking for this user] as in [S-032_CreateDataForNewBookings],
    And a successful call [to post judicial assignments refresh request on ORM] as in [S-032_PostRefreshRequestToORM],
    And the request [contains the actorIds of the user having existing judicial role assignments],
    And the request [contains the actorIds of the user for which above booking is created],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-032_DeleteDataForRoleAssignments]

  @S-033 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mappings for User having no judicial booking
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-033_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [S-033_DeleteDataForBookings],
    And a successful call [to post judicial assignments refresh request on ORM] as in [S-033_PostRefreshRequestToORM],
    And the request [contains the actorIds of the user having existing judicial role assignments],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-033_DeleteDataForRoleAssignments]

#  Expected Roles: fee-paid-judge and hmcts-judiciary

