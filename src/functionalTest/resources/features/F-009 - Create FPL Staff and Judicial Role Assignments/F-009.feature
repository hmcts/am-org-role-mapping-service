@F-009
Feature: F-009 : Create Role Assignments for PublicLaw Caseworker and Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-091
  @FeatureToggle(DB:publiclaw_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker and Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-091_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-091_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-091_DeleteDataForRoleAssignments].

  @S-092
  @FeatureToggle(DB:publiclaw_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader and Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-092_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-092_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-092_DeleteDataForRoleAssignments].

  @S-093
  @FeatureToggle(DB:publiclaw_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader and CTSC Admin
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-093_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-093_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role],
    And the request [contains the actorId of the user just published who has CTSC Admin],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-093_DeleteDataForRoleAssignments].

  @S-094
  @FeatureToggle(DB:publiclaw_hearing_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Deputy District Judge - Fee-Paid appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-094_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-094_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-094_DeleteDataForRoleAssignments].

  @S-095
  @FeatureToggle(DB:publiclaw_hearing_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Designated Family Judge - Salaried appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-095_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-095_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-095_DeleteDataForRoleAssignments].

  @S-009.02
  @FeatureToggle(DB:publiclaw_wa_1_7=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker (ABA3 FPL)] as in [S-009.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-009_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-009.02a
  @FeatureToggle(DB:publiclaw_wa_1_7=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker + Task Supervisor (ABA3 FPL)] as in [S-009.02a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-009_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
