@F-005
Feature: F-005 : Create SSCS Role Assignments for Hearing Roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-041
  @FeatureToggle(DB:sscs_wa_1_5=on) @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for SSCS other government department role assignments (listed-hearing-viewer)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for DWP Caseworker] as in [S-041__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-041_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-041_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published for other government department role assignments],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-041_DeleteDataForRoleAssignments].

  @S-042
  @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for SSCS admin role assignments (hearing-viewer, hearing-manager)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Hearing Centre Administrator] as in [S-042__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-042_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-042_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published for admin role assignments],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-042_DeleteDataForRoleAssignments].

  @S-043
  @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for SSCS legal operation role assignments (hearing-viewer, hearing-manager)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Regional Centre Team Leader] as in [S-043__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-043_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-043_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published for legal operation role assignments],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-043_DeleteDataForRoleAssignments].

  @S-044
  @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for SSCS CTSC role assignments (hearing-viewer, hearing-manager)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify caseworker details for Legal Caseworker] as in [S-044__VerifyCaseworkerDetails],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-044_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-044_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published for CTSC role assignments],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-044_DeleteDataForRoleAssignments].

  @S-045
  @FeatureToggle(DB:sscs_wa_1_0=on) @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create organisational role mapping for SSCS judicial role assignments (hearing-viewer)
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-045_DeleteDataForRoleAssignments]
    And a successful call [to post create organisational role mapping request on ORM] as in [S-045_PostCreateRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user just published for judicial role assignments],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-045_DeleteDataForRoleAssignments].
