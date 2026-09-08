@F-008_staff @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
Feature: F-008 : Create PrivateLaw Staff Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-008.01
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker (ABA5 PRL)] as in [S-008.01__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.01a
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Task Supervisor (ABA5 PRL)] as in [S-008.01a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Task Supervisor],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.01b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Senior Legal Caseworker + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Senior Legal Caseworker + Case allocator (ABA5 PRL)] as in [S-008.01b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.02
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker (ABA5 PRL)] as in [S-008.02__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.02b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Legal Caseworker + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker + Case allocator (ABA5 PRL)] as in [S-008.02b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Legal Caseworker role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.03
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader (ABA5 PRL)] as in [S-008.03__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.03a
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Task Supervisor (ABA5 PRL)] as in [S-008.03a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Task Supervisor],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.03b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Hearing Centre Team Leader + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Team Leader + Case allocator (ABA5 PRL)] as in [S-008.03b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Hearing Centre Team Leader role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.04
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator (ABA5 PRL)] as in [S-008.04__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.04b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Hearing Centre Administrator + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator + Case allocator (ABA5 PRL)] as in [S-008.04b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Hearing Centre Administrator role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.09
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader (ABA5 PRL)] as in [S-008.09__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has CTSC Team Leader role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.09a
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Task Supervisor
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Task Supervisor (ABA5 PRL)] as in [S-008.09a__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Task Supervisor],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.09b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for CTSC Team Leader + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Team Leader + Case allocator (ABA5 PRL)] as in [S-008.09b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has CTSC Team Leader role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.10
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for CTSC Admin
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin (ABA5 PRL)] as in [S-008.10__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has CTSC Admin role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.10b
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for CTSC Admin + Case allocator
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for CTSC Admin + Case allocator (ABA5 PRL)] as in [S-008.10b__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has CTSC Admin role + Case allocator],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].


  @S-008.18
  @FeatureToggle(DB:privatelaw_wa_1_5=on)
  Scenario: must successfully create org role mapping for Cafcass Cymru Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Cafcass Cymru Caseworker (ABA5 PRL)] as in [S-008.18__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [DeleteDataForStaffRoleAssignments_RasV1],
    And a successful call [to publish existing CRD user ids to endpoint] as in [PushMessageToCRDProfileProcess_RasV1],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published who has Cafcass Cymru Caseworker role],
    And it is submitted to call the [Query Role Assignments] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [DeleteDataForStaffRoleAssignments_RasV1].

