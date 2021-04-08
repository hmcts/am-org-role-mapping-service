@F-002
Feature: Create Role Assignments for JRD Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-011 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mapping for a multiple user having single role
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments01],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments02],
    And a successful call [to publish existing JRD user ids to topic] as in [S-011_PushMessageToAzureServiceBus],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments01],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments02].

  @S-012 @FeatureToggle(orm-jrd-org-role)
  #At the moment only one role, later will support multiple roles.
  Scenario: must successfully create judicial role mapping for a user having multiple roles
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-012_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing JRD user ids to topic] as in [S-012_PushMessageToAzureServiceBus],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-012_DeleteDataForRoleAssignments01],

  @S-013 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully generate duplicate records message
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-013_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing JRD user ids to topic] as in [S-013_PushMessageToAzureServiceBus],
    And a successful call [to publish existing JRD user ids to topic] as in [S-013_PushMessageToAzureServiceBus],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-013_DeleteDataForRoleAssignments01],

#  @S-003 @FeatureToggle(orm-jrd-org-role)
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
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-006_DeleteDataForRoleAssignments01].
#
#  @S-004 @FeatureToggle(orm-jrd-org-role)
#  Scenario: must successfully generate duplicate records message
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-007_DeleteDataForRoleAssignments01],
#    And a successful call [to publish an existing CRD user ids to topic] as in [S-007_PushMessageToAzureServiceBus],
#    And a successful call [to publish an existing CRD user ids to topic] as in [S-007_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorId of the user just published],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-007_DeleteDataForRoleAssignments01].
