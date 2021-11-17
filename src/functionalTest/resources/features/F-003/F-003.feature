@F-003
Feature: F-003 : Create Role Assignments for Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-021 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mapping for President of Tribunal appointment with Resident Immigration Judge role
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-021_DeleteDataForRoleAssignments01],
     And a successful call [to publish existing JRD user ids to endpoint] as in [S-021_PushMessageToJRDService],
     And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
     And the request [contains the actorIds of the user just published],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-021_DeleteDataForRoleAssignments01]

  @S-022 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mapping for Tribunal Judge - fee paid appointment with Assistant Resident Judge role.
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-022_DeleteDataForRoleAssignments01],
     And a successful call [to publish existing JRD user ids to endpoint] as in [S-022_PushMessageToJRDService],
     And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
     And the request [contains the actorIds of the user just published],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-022_DeleteDataForRoleAssignments01].

  @S-023 @FeatureToggle(orm-jrd-org-role)
  Scenario: must successfully create judicial role mapping for Tribunal Judge - salaried appointment with Designated Immigration Judge role.
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-023_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-023_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-023_DeleteDataForRoleAssignments01].

#      @S-024 @FeatureToggle(orm-jrd-org-role)
#          Scenario: should create role assignments when role id and contract type are  84 and 5
#            Given a user with [an active IDAM profile with full permissions],
#            And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-024_DeleteDataForRoleAssignments01],
#            And a successful call [to publish existing JRD user ids to topic] as in [S-024_PushMessageToAzureServiceBus],
#            And the request [contains the JRD user role id is not 84],
#            And the request [contains the JRD user contract type is not 5],
#            And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#            And the request [contains the actorIds of the user just published],
#            When a request is prepared with appropriate values,
#            And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#            Then a positive response is received,
#            And the response has all other details as expected,
#            And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-024_DeleteDataForRoleAssignments01].