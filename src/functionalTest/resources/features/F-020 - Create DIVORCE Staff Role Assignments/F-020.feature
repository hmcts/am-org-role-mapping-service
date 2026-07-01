@F-020
Feature: F-020 : Create Divorce Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-020.01
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (Divorce)] as in [S-020.01__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.01a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (Divorce)] as in [S-020.01a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.01b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case Allocator (Divorce)] as in [S-020.01b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.02
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Admin (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Admin (Divorce)] as in [S-020.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Admin (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.02a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Admin + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Admin + Task Supervisor (Divorce)] as in [S-020.02a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Admin role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.02b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Admin + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Admin + Case Allocator (Divorce)] as in [S-020.02b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Admin role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.03
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Team Leader (Divorce)] as in [S-020.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.03a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Team Leader + Task Supervisor (Divorce)] as in [S-020.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.03b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Team Leader + Case Allocator (Divorce)] as in [S-020.03b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.04
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Admin (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Admin (Divorce)] as in [S-020.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Admin (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.04a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Admin + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Admin + Task Supervisor (Divorce)] as in [S-020.04a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Admin role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.04b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for NBC Admin + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for NBC Admin + Case Allocator (Divorce)] as in [S-020.04b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has NBC Admin role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.05
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader (Divorce)] as in [S-020.05__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.05a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Task Supervisor (Divorce)] as in [S-020.05a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.05b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Case Allocator (Divorce)] as in [S-020.05b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.06
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Admin (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin (Divorce)] as in [S-020.06__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.06a
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Task Supervisor (Divorce)] as in [S-020.06a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-020.06b
  @FeatureToggle(DB:fr_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Case Allocator (Divorce)] as in [S-020.06b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
