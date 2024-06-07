@F-002
Feature:F-002: Refresh Role Assignments for CRD and JRD users

  Background:
    Given an appropriate test context as detailed in the test data source

   @S-011
   @FeatureToggle(EV:CASEWORKER_FTA_ENABLED=on)
   @FeatureToggle(orm-refresh-role) @FeatureToggle(EV:REFRESH_FTA_ENABLED=on)
   Scenario: must successfully refresh staff user org roles for a job
     Given a user with [an active IDAM profile with full permissions],
     And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [S-011_InsertJobForRefreshAPI],
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments]
     And the request [contains an existing job details],
     When a request is prepared with appropriate values,
     And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
     Then a positive response is received,
     And the response has all other details as expected,
     And a wait time of [10] seconds [to allow for service bus to process the request],
     And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-011_FetchJobDetailsFromORM],
     And a successful call [to delete job details from ORM DB] as in [S-011_DeleteJobFromORMDB],
     And a successful call [to fetch role assignments from Role Assignment Service] as in [S-011_FetchRoleAssignments],
     And the response has all other details as expected,
     And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-011_DeleteDataForRoleAssignments].

  @S-012
  @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
  @FeatureToggle(orm-refresh-role) @FeatureToggle(EV:REFRESH_FTA_ENABLED=on)
  Scenario: must successfully refresh judicial user org roles for a job
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [S-012_InsertJobForRefreshAPI],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-012_DeleteDataForRoleAssignments]
    And the request [contains an existing job details],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a wait time of [10] seconds [to allow for service bus to process the request],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-012_FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [S-012_DeleteJobFromORMDB],
    And a successful call [to fetch role assignments from Role Assignment Service] as in [S-012_FetchRoleAssignments],
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [S-012_DeleteDataForRoleAssignments].
