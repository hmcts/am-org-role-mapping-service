@F-001 @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-001 : Create Role Assignments for Caseworker Users

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

  @S-001.01
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Tribunal Case Worker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Tribunal Case Worker (BFA1 IA)] as in [S-001.01__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Tribunal Case Worker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.01a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Tribunal Case Worker + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Tribunal Case Worker + Task Supervisor (BFA1 IA)] as in [S-001.01a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Tribunal Case Worker role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.01b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Senior Tribunal Case Worker + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Tribunal Case Worker + Case allocator (BFA1 IA)] as in [S-001.01b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Senior Tribunal Case Worker role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.02
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Tribunal Case Worker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Tribunal Case Worker (BFA1 IA)] as in [S-001.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Tribunal Case Worker role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.02a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Tribunal Case Worker + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Tribunal Case Worker + Task Supervisor (BFA1 IA)] as in [S-001.02a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Tribunal Case Worker role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.02b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Tribunal Case Worker + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Tribunal Case Worker + Case allocator (BFA1 IA)] as in [S-001.02b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Tribunal Case Worker role + Case allocator],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

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

  @S-001.03a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (BFA1 IA)] as in [S-001.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.03b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case allocator (BFA1 IA)] as in [S-001.03b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Case allocator],
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

  @S-001.04a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Task Supervisor (BFA1 IA)] as in [S-001.04a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.04b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for Hearing Centre Administrator + Case allocator (BFA1 IA)] as in [S-001.04b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Case allocator],
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

  @S-001.05a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Court Clerk + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Court Clerk + Task Supervisor (BFA1 IA)] as in [S-001.05a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Court Clerk role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.05b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for Court Clerk + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for Court Clerk + Case allocator (BFA1 IA)] as in [S-001.05b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has Court Clerk role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.06
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Team leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Team leader (BFA1 IA)] as in [S-001.06__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.06a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Team leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Team leader + Task Supervisor (BFA1 IA)] as in [S-001.06a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Team leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.06b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Team leader + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for National Business Centre Team leader + Case allocator (BFA1 IA)] as in [S-001.06b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has National Business Centre Team leader role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.07
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Listing team
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Listing team (BFA1 IA)] as in [S-001.07__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Listing team role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.07a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Listing team + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Listing team + Task Supervisor (BFA1 IA)] as in [S-001.07a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Listing team role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.07b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Listing team + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for National Business Centre Listing team + Case allocator (BFA1 IA)] as in [S-001.07b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has National Business Centre Listing team role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.08
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Payments Team
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Payments Team (BFA1 IA)] as in [S-001.08__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Payments Team role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.08a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Payments Team + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for National Business Centre Payments Team + Task Supervisor (BFA1 IA)] as in [S-001.08a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has National Business Centre Payments Team role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.08b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for National Business Centre Payments Team + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for National Business Centre Payments Team + Case allocator (BFA1 IA)] as in [S-001.08b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has National Business Centre Payments Team role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.09
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC team leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC team leader (BFA1 IA)] as in [S-001.09__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC team leader role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.09a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC team leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC team leader + Task Supervisor (BFA1 IA)] as in [S-001.09a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC team leader role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.09b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC team leader + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for CTSC team leader + Case allocator (BFA1 IA)] as in [S-001.09b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has CTSC team leader role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.10
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Administrator (BFA1 IA)] as in [S-001.10__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.10a
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Administrator + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Administrator + Task Supervisor (BFA1 IA)] as in [S-001.10a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
    And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has CTSC Administrator role + Task Supervisor],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].

  @S-001.10b
  @FeatureToggle(DB:iac_wa_1_2=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create org role mapping for CTSC Administrator + Case allocator
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to verify caseworker details for CTSC Administrator + Case allocator (BFA1 IA)] as in [S-001.10b__VerifyCaseworkerDetails],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments],
     And a successful call [to publish existing CRD user ids to endpoint] as in [F-001_PushMessageToCRDService],
     And the request [contains the actorId of the user just published who has CTSC Administrator role + Case allocator],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
     Then a positive response is received,
     And the response has all other details as expected
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForRoleAssignments].
