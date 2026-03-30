@F-017
Feature: F-017 : Create Probate Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-017.01
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker (Probate)] as in [S-017.01__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.01a
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Task Supervisor (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Task Supervisor (Probate)] as in [S-017.01a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.01b
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Case Allocator (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Case Allocator (Probate)] as in [S-017.01b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Case Allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.02
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker (Probate)] as in [S-017.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.02a
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker + Task Supervisor (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker + Task Supervisor (Probate)] as in [S-017.02a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.02b
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker + Case Allocator (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker + Case Allocator (Probate)] as in [S-017.02b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role + Case Allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.03
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader (Probate)] as in [S-017.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.03a
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Task Supervisor (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Task Supervisor (Probate)] as in [S-017.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.03b
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Case Allocator (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Case Allocator (Probate)] as in [S-017.03b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Case Allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-017.04
  @FeatureToggle(DB:probate_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Admin (Probate)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin (Probate)] as in [S-017.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-017_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
