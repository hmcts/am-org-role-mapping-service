@F-015
Feature: F-015 : Create HRS Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-015.22
  @FeatureToggle(DB:hrs_1_0=off) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for HRS Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for HRS Team Leader (HRS)] as in [S-015.22__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-015_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has HRS Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
