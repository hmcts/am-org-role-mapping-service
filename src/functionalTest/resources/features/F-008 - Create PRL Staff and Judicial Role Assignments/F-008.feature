@F-008
Feature: F-008 : Create Role Assignments for PrivateLaw Caseworker and Judicial Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-071
  @FeatureToggle(DB:privatelaw_wa_1_1=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Legal Caseworker and Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-071_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-071_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-071_DeleteDataForRoleAssignments].


  @S-072
  @FeatureToggle(DB:privatelaw_wa_1_0=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader and Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-072_DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-072_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-072_DeleteDataForRoleAssignments].


  @S-073
  @FeatureToggle(DB:privatelaw_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for Deputy Circuit Judge - fee-paid appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-073_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-073_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-073_DeleteDataForRoleAssignments].


  @S-074
  @FeatureToggle(DB:privatelaw_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mapping for District Judge - salaried appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-074_DeleteDataForRoleAssignments],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-074_PushMessageToJRDService],
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-074_DeleteDataForRoleAssignments].

  @S-008.04
  @FeatureToggle(DB:privatelaw_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator (ABA5 PRL)] as in [S-008.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-008.04b
@FeatureToggle(DB:privatelaw_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Scenario: must successfully create org role mapping for Hearing Centre Administrator + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Case allocator (ABA5 PRL)] as in [S-008.04b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-008.02
@FeatureToggle(DB:privatelaw_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Scenario: must successfully create org role mapping for Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    #And a successful call [to verify caseworker details for Legal Caseworker (ABA5 PRL)] as in [S-008.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-008.02b
@FeatureToggle(DB:privatelaw_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Scenario: must successfully create org role mapping for Legal Caseworker + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker + Case allocator (ABA5 PRL)] as in [S-008.02b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role + Case allocator] ,
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-008.10
Scenario: must successfully create org role mapping for CTSC Admin
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin (ABA5 PRL)] as in [S-008.10__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

@S-008.10b
Scenario: must successfully create org role mapping for CTSC Admin + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Case allocator (ABA5 PRL)] as in [S-008.10b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-008_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Admin role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
