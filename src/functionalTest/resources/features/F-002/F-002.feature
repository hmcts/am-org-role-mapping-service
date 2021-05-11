#@F-002
#Feature: Refresh Role Assignments for CRD users
#
#  Background:
#    Given an appropriate test context as detailed in the test data source
#
#  @S-011 @FeatureToggle(orm-base-flag)
#  Scenario: must successfully refresh org role mapping for complete success
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to Refresh API] as in [S-011_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#
#  @S-012 @FeatureToggle(orm-base-flag)
#  Scenario: must successfully refresh org role mapping for partial success
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to Refresh API] as in [S-012_PushMessageToAzureServiceBus],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    And the request [contains few actorIds have data issues which leads to aborted],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#
#  @S-013 @FeatureToggle(orm-base-flag)
#  Scenario: must successfully refresh org role mapping for list of users which were failed earlier
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to Refresh API] as in [S-013_PushMessageToAzureServiceBus],
#    And the request [contains the actorIds which were failed earlier and fixed now],
#    And a successful call [to provide adequate time for RAS to create records] as in [WaitForRASProcessing],
#    And the request [contains the actorIds of the user just published],
#    When a request is prepared with appropriate values,
#    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
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
