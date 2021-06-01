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
  Scenario: must not refresh org roles for a job with non existent failed userIds
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to insert new job in ORM DB to initiate Refresh process] as in [InsertJobForRefreshAPI],
    And the request [contains an existing job details with suspended/nonexistent userIds],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh API] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to provide adequate time for RAS to create records] as in [WaitFor20sRASProcessing],
    And a successful call [to fetch job details from ORM DB to validate Refresh process] as in [S-013_FetchJobDetailsFromORM],
    And a successful call [to delete job details from ORM DB] as in [DeleteJobFromORMDB].
#
#
#  @S-014 @FeatureToggle(orm-base-flag) Integration Test
#  Scenario: must execute the retry mechanism
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to Refresh API] as in [S-014_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    And the request [has some server issues and retry until 3 attempts],
#    And the request [marked as aborted if any one of the user id failed],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-015 @FeatureToggle(orm-base-flag) Integration Test
#  Scenario: must verify the Live, History and Request tables records
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to Refresh API] as in [S-015_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-016
#  Scenario: Swagger UI should be up and running
#    User able to execute the Refresh endpoint through Swagger UI
