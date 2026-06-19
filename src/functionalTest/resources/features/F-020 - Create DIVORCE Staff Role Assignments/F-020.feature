@F-020 @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-020 : Create DIVORCE Staff Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-020.03
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy CTSC Team Leader caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.04
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Task Supervisor + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy CTSC Admin caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.05
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy Hearing Centre Team Leader caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.06
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Task Supervisor + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy Hearing Centre Administrator caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.07
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Team Leader (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy National Business Centre Team Leader caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team Leader (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-020.08
  @FeatureToggle(DB:divorce_wa_1_0=on)
  Scenario: must successfully create org role mapping for National Business Centre Administrator + Task Supervisor + Case Allocator (Divorce)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create the Financial Remedy National Business Centre Administrator caseworker profile] as in [F-020_CreateCaseworkerProfileInCRD],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-020_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Administrator (Divorce) role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
