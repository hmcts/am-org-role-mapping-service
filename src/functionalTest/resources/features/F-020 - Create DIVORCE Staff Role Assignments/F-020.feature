@F-020 @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-020 : Create DIVORCE Staff Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-020.03
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader (ABA2 DIVORCE)] as in [S-020.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.03a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Task Supervisor (ABA2 DIVORCE)] as in [S-020.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.03b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Case Allocator (ABA2 DIVORCE)] as in [S-020.03b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-020.04
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Admin (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin (ABA2 DIVORCE)] as in [S-020.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.04a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Task Supervisor (ABA2 DIVORCE)] as in [S-020.04a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.04b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Case Allocator (ABA2 DIVORCE)] as in [S-020.04b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-020.05
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (ABA2 DIVORCE)] as in [S-020.05__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.05a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (ABA2 DIVORCE)] as in [S-020.05a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.05b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case Allocator (ABA2 DIVORCE)] as in [S-020.05b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-020.06
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator (ABA2 DIVORCE)] as in [S-020.06__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.06a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Task Supervisor (ABA2 DIVORCE)] as in [S-020.06a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.06b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Case Allocator (ABA2 DIVORCE)] as in [S-020.06b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-020.07
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Team Leader (ABA2 DIVORCE)] as in [S-020.07__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.07a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Team Leader + Task Supervisor (ABA2 DIVORCE)] as in [S-020.07a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team Leader role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.07b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Team Leader + Case Allocator (ABA2 DIVORCE)] as in [S-020.07b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team Leader role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-020.08
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Administrator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Administrator (ABA2 DIVORCE)] as in [S-020.08__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Administrator (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.08a
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Administrator + Task Supervisor (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Administrator + Task Supervisor (ABA2 DIVORCE)] as in [S-020.08a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Administrator role + Task Supervisor (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.08b
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Administrator + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Administrator + Case Allocator (ABA2 DIVORCE)] as in [S-020.08b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Administrator role + Case Allocator (Divorce)],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
