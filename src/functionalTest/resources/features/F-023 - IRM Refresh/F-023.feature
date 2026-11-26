@F-023
Feature: F-023 : IRM Refresh

  Background:
    Given an appropriate test context as detailed in the test data source

  # Copy of Judicial Booking Refresh (F-004 > S-031)
  @S-023.01
  @FeatureToggle(EV:IRM_JUDICIAL_FTA_ENABLED=on)
  Scenario: must successfully create irm queue entry based on judicial role assignments for a user having single judicial booking
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-023.01__DeleteDataForRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [S-023.01__DeleteIrmQueueEntryForUser],
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [F-023_DeleteDataForBookings],
    And a successful call [to create single booking for this user] as in [F-023_CreateDataForBookings],
    And a successful call [to refresh a judicial user's role assignments] as in [F-023_PostRefreshRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user having existing judicial role assignments],
    And the request [contains the actorId of the user for which above booking is created],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response [contains a bookable fee-paid-judge role-assignment],
    And the response [contains one booked judge/fee-paid role-assignment],
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-023.01__DeleteDataForRoleAssignments],
    And a successful call [to verify user from IRM Queue] as in [S-023.01__VerifyIrmQueueEntryForUser],
    And a successful call [to delete user from IRM Queue] as in [S-023.01__DeleteIrmQueueEntryForUser].

  # Copy of Judicial ASB (F-011 > S-213)
  @S-023.02
  @FeatureToggle(EV:IRM_JUDICIAL_FTA_ENABLED=on)
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=on) @FeatureToggle(DB:employment_wa_1_0=on)
  Scenario: must successfully create judicial role mapping for Tribunal Member - Fee Paid appointment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForASBRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [S-023.02__DeleteIrmQueueEntryForUser],
    And a successful call [to publish existing JRD user ids to endpoint] as in [S-023.02__PushMessageToJRDService],
    And the request [contains the actorIds of the user just published],
    And a wait time of [5] seconds [to allow for service bus to process the request]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForASBRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [S-023.02__DeleteIrmQueueEntryForUser].

  # Copy of Caseworker ASB (F-011 > S-211)
  @S-023.03
  @FeatureToggle(EV:IRM_CASEWORKER_FTA_ENABLED=on)
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=on) @FeatureToggle(DB:employment_wa_1_0=on)
  Scenario: must successfully create org role mapping for Legal Caseworker and Senior Legal Caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForASBRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForActor],
    And a successful call [to publish existing CRD user ids to endpoint] as in [S-023.03__PushMessageToCRDService],
    And the request [contains the actorId of the user just published who has Legal Caseworker role],
    And the request [contains the actorId of the user just published who has Senior Legal Caseworker role],
    And a wait time of [5] seconds [to allow for service bus to process the request]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForASBRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForActor].

  # Copy of Judicial Refresh Job (F-002 > S-012)
  @S-023.04
  @FeatureToggle(EV:IRM_JUDICIAL_FTA_ENABLED=on)
  @FeatureToggle(EV:REFRESH_FTA_ENABLED=on)
  Scenario: must successfully refresh judicial user org roles for a job
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [F-023_InsertJudicialJobForRefreshAPI],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForUser],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForRefreshRoleAssignments],
    And the request [contains an existing job details],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a wait time of [10] seconds [to allow async call to process the request],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-023.04__FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [S-023.04__DeleteJobFromORMDB],
    And a successful call [to fetch role assignments from Role Assignment Service] as in [S-023.04__FetchRoleAssignments],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForRefreshRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForUser].

  # Copy of Judicial Refresh Job (F-002 > S-011)
  @S-023.05
  @FeatureToggle(EV:IRM_CASEWORKER_FTA_ENABLED=on)
  @FeatureToggle(EV:REFRESH_FTA_ENABLED=on)
  Scenario: must successfully refresh staff user org roles for a job
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [F-023_InsertCaseworkerJobForRefreshAPI],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForUser],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForRefreshRoleAssignments],
    And the request [contains an existing job details],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a wait time of [10] seconds [to allow async call to process the request],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-023.05__FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [S-023.05__DeleteJobFromORMDB],
    And a successful call [to fetch role assignments from Role Assignment Service] as in [S-023.05__FetchRoleAssignments],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-023_DeleteDataForRefreshRoleAssignments],
    And a successful call [to delete user from IRM Queue] as in [F-023_DeleteIrmQueueEntryForUser].