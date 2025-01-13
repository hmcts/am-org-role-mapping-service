@F-001 @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-001 :Create Role Assignments for Caseworker Users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-002
  @FeatureToggle(DB:iac_1_1=on)
  @Retryable(maxAttempts=3,delay=500,statusCodes={502,503,504})
  Scenario: must successfully create org role mapping for a multiple user having single role
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments01],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments02],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-002_PushMessageToCRDService],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments01],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-002_DeleteDataForRoleAssignments02].

  @S-005
  @FeatureToggle(DB:iac_1_1=on)
  Scenario: should not create role assignments when user primary work location is false
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-005_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-005_PushMessageToCRDService],
    And the request [contains the CRD user primary work location set to false],
    And the request [contains the actorIds of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-005_DeleteDataForRoleAssignments01].

  @S-006
  @FeatureToggle(DB:iac_1_1=on)
  Scenario: must successfully create org role mapping for a user having multiple roles
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-006_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-006_PushMessageToCRDService],
    And the request [contains the actorId of the user just published],
    And the request [contains multiple role assignments for the user],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-006_DeleteDataForRoleAssignments01].

  @S-007
  @FeatureToggle(DB:iac_1_1=on)
  Scenario: must successfully generate duplicate records message
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-007_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-007_PushMessageToCRDService],
    And the request [contains the actorId of the user just published],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Push User Ids to CRD endpoint to process] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-007_DeleteDataForRoleAssignments01].

  @S-010
  @FeatureToggle(DB:iac_1_1=on)
  Scenario: must successfully create org role mapping for CA and TS roles
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-010_DeleteDataForRoleAssignments01],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-010_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CaseAllocator role],
    And the request [contains the actorId of the user just published who has TaskSupervisor role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-010_DeleteDataForRoleAssignments01].

  @S-001.03
  @FeatureToggle(DB:iac_wa_1_3=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (BFA1 IA)] as in [S-001.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.04
  @FeatureToggle(DB:iac_wa_1_3=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator (BFA1 IA)] as in [S-001.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.05
  @FeatureToggle(DB:iac_wa_1_3=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Court Clerk
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Court Clerk (BFA1 IA)] as in [S-001.05__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Court Clerk role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
