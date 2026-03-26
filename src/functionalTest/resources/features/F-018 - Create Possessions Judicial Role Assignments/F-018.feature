@F-003 @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
Feature: F-018 : Create Role Assignments for Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-018.01
  @FeatureToggle(DB:possessions_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Generic Fee Paid appointment in Possessions (without booking)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-018__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-018__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Generic Fee Paid appointment in Possessions],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments].

  @S-018.02
  @FeatureToggle(DB:possessions_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Possession Leadership Judge Salaried appointment in Possessions
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-018__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-018__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Possession Leadership Judge Salaried appointment in Possessions],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments].

  @S-018.03
  @FeatureToggle(DB:possessions_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Possession Circuit Judge Salaried appointment in Possessions
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-018__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-018__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Possession Circuit Judge Salaried appointment in Possessions],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments].

  @S-018.04
  @FeatureToggle(DB:possessions_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Possession Circuit Judge Fee Paid appointment in Possessions (without booking)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-018__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-018__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Possession Circuit Judge Fee Paid appointment in Possessions],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments].

  @S-018.05
  @FeatureToggle(DB:possessions_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for any Possession Salaried appointment in Possessions
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments],
    And a successful call [to snapshot judicial details] as in [F-018__SnapshotJudicialDetails],
    And a successful call [to publish existing JRD user ids to endpoint] as in [F-018__PushMessageToJRDService],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has any Possession Salaried appointment in Possessions],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-018__DeleteDataForRoleAssignments].
