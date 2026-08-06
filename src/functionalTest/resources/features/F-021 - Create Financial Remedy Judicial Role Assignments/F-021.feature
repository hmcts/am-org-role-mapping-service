@F-021 @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
Feature: F-021 : Create Financial Remedy Judicial Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-021.01
  @FeatureToggle(DB:fr_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Generic Fee Paid appointment in Financial Remedy (without booking)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-021__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-021__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Generic Fee Paid appointment in Financial Remedy],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments].

  @S-021.02
  @FeatureToggle(DB:fr_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Generic Salaried Judge appointment in Financial Remedy
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-021__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-021__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Generic Salaried Judge appointment in Financial Remedy],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments].

  @S-021.03
  @FeatureToggle(DB:fr_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Generic Leadership Judge appointment in Financial Remedy
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-021__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-021__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Generic Leadership Judge appointment in Financial Remedy],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-021__DeleteDataForRoleAssignments].
