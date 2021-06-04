@F-002
Feature: Refresh Role Assignments for CRD users

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-011
  Scenario: must successfully refresh org roles for a job without failed userIds
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [InsertJobForRefreshAPI],
    And the request [contains an existing job details],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [DeleteJobFromORMDB].


  @S-012
  Scenario: must successfully refresh org roles for a job with failed userIds
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job with aborted status in ORM DB] as in [S-012_InsertJobWithAbortedStatus],
    And a successful call [to insert new job with linked Ids in ORM DB] as in [S-012_InsertJobWithNewStatusAndLinkedId],
    And the request [contains an existing job details with failed userIds from aborted job],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-012_FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [S-012_DeleteJobFromORMDB_1],
    And a successful call [to delete job details from ORM DB] as in [S-012_DeleteJobFromORMDB_2].


  @S-013
  Scenario: must set the refresh job to aborted for a non existent failed userIds
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [InsertJobForRefreshAPI],
    And the request [contains an existing job details with suspended/nonexistent userIds],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to provide adequate time for RAS to create records] as in [WaitFor20sRASProcessing],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-013_FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [S-013_DeleteJobFromORMDB].

