@F-011
Feature: F-011 : Verify CRD and JRD service bus messages

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-211
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=on)
  Scenario: must successfully verify CRD service bus messages for Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-211_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-211_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    And a wait time of [5] seconds [to allow for service bus to process the request]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-211_DeleteDataForRoleAssignments].

  @S-213
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Tribunal Member - Fee Paid appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-213_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-213_PushMessageToJRDService],
    And the request [contains the actorIds of the user just published],
    And a wait time of [5] seconds [to allow for service bus to process the request]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-213_DeleteDataForRoleAssignments].
