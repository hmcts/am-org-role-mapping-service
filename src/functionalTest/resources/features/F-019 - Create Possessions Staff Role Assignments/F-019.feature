@F-019 @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-019 : Create Possessions Staff Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-019.03
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (AAA3 PCS)] as in [S-019.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.03a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (AAA3 PCS)] as in [S-019.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.03b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case allocator (AAA3 PCS)] as in [S-019.03b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.04
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator (AAA3 PCS)] as in [S-019.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.04a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Task Supervisor (AAA3 PCS)] as in [S-019.04a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.04b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Case allocator (AAA3 PCS)] as in [S-019.04b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.09
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader (AAA3 PCS)] as in [S-019.09__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.09a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Task Supervisor (AAA3 PCS)] as in [S-019.09a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.09b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Case allocator (AAA3 PCS)] as in [S-019.09b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.10
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Administrator (AAA3 PCS)] as in [S-019.10__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.10a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Administrator + Task Supervisor (AAA3 PCS)] as in [S-019.10a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.10b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for CTSC Administrator + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Administrator + Case allocator (AAA3 PCS)] as in [S-019.10b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.20
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Administrator (AAA3 PCS)] as in [S-019.20__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.20a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Administrator + Task Supervisor (AAA3 PCS)] as in [S-019.20a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.20b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Administrator+ Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Administrator + Case allocator (AAA3 PCS)] as in [S-019.20b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Administrator role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.21
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Team Leader (AAA3 PCS)] as in [S-019.21__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.21a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Team Leader + Task Supervisor (AAA3 PCS)] as in [S-019.21a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Team Leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.21b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for WLU Team Leader+ Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for WLU Team Leader + Case allocator (AAA3 PCS)] as in [S-019.21b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has WLU Team Leader role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].


  @S-019.23
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Bailiff Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Bailiff Administrator (AAA3 PCS)] as in [S-019.23__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Bailiff Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.23a
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Bailiff Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Bailiff Administrator + Task Supervisor (AAA3 PCS)] as in [S-019.23a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Bailiff Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-019.23b
  @FeatureToggle(DB:possessions_wa_1_0=off)
  Scenario: must successfully create org role mapping for Bailiff Administrator + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Bailiff Administrator + Case allocator (AAA3 PCS)] as in [S-019.23b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-019__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Bailiff Administrator role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
