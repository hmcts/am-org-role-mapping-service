@F-001
Feature: Create Role Assignments for Users

  Background:
    Given an appropriate test context as detailed in the test data source

#  @S-002 @FeatureToggle(orm-base-flag)
#  Scenario: must successfully create org role mapping for a multiple user having single role
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments01],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments02],
#    And a successful call [to publish an existing CRD user ids to topic] as in [S-002_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments01],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments02].

  @S-005 @FeatureToggle(orm-base-flag)
  Scenario: should not create role assignments when user primary work location is false
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-005_DeleteDataForRoleAssignments01],
    And a successful call [to publish an existing CRD user ids to topic] as in [S-005_PushMessageToAzureServiceBus],
    And the request [contains the CRD user primary work location set to false],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-005_DeleteDataForRoleAssignments01],

#  @S-006 @FeatureToggle(orm-base-flag)
#  Scenario: must successfully create org role mapping for a user having multiple roles
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-006_DeleteDataForRoleAssignments01],
#    And a successful call [to publish an existing CRD user ids to topic] as in [S-006_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorId of the user just published],
#    And the request [contains multiple role assignments for the user],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-006_DeleteDataForRoleAssignments01],

  #  @S-002 @FeatureToggle(orm-base-flag)
  #  Scenario: must successfully generate duplicate records message
  #    Given a user with [an active IDAM profile with full permissions],
  #    When a request is prepared with appropriate values,
  #    And it is submitted to call the [Create Role Assignments] operation of [Organisation Role Mapping Service],
  #    And the request [prepared to submit second time with no changes],
  #    And it is submitted to call the [Create Role Assignments] operation of [Organisation Role Mapping Service],
  #    Then a positive response is received,
  #    And the response has all other details as expected,
  #    And a successful call [to delete role assignments just created above] as in [S-002_DeleteDataForRoleAssignments].

  #Will implement this once ORM integrated with CRD API.
  #  @S-006
  #  Scenario: must successfully delete org role mapping when delete flag is true
  #    Given a user with [an active IDAM profile with full permissions],
  #    And a successful call [to get corresponding appointment data from CRD API] as in [Get_Data_from_CRD_API],
  #    When a request is prepared with appropriate values,
  #    And the request [contains the data received from CRD API got transformed],
  #    And the request [contains delete flag is true and has empty assignment records],
  #    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
  #    Then a positive response is received,
  #    And the response has all other details as expected.

  #Will implement this once ORM integrated with CRD API.
  #  @S-009
  #  Scenario: must successfully create org role mapping for an update of role TCW to STCW
  #    Given a user with [an active IDAM profile with full permissions],
  #    And a successful call [to get corresponding appointment data from CRD API] as in [Get_Data_from_CRD_API],
  #    When a request is prepared with appropriate values,
  #    And the request [contains an update of role CTW to SCTW],
  #    And the request [contains a single user with multiple role assignments],
  #    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
  #    Then a positive response is received,
  #    And the response has all other details as expected,
  #    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  #For delete flag, user has to be in Active Status in both UserProfile and IDAM tables.
  #  @S-004 @FeatureToggle(orm-base-flag)
  #  Scenario: must successfully remove role assignments using delete flag true
  #    Given a user with [an active IDAM profile with full permissions],
  #    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-004_DeleteDataForRoleAssignments01],
  #    And a successful call [to publish an existing CRD user ids to topic] as in [S-004_PushMessageToAzureServiceBus],
  #    And the request [contains the CRD user whose delete flag set to true],
  #    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
  #    And the request [contains the actorId of the user just published],
  #    When a request is prepared with appropriate values,
  #    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
  #    Then a positive response is received,
  #    And the response has all other details as expected,
  #    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-004_DeleteDataForRoleAssignments01],
