@F-013
Feature: F-013 : PRM Scheduler

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-013.01
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 1 - Find And Update Case Definition Changes
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find And Update Case Definition Changes] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify the SOLICITOR_PROFILE exists in the refresh queue] as in [S-013.01_VerifySolicitorProfileRefreshQueueValueExists]

  @S-013.02
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 2 - Find Organisations With Stale Profiles
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the SOLICITOR_PROFILE refresh queue value active] as in [S-013.02_MakeSolicitorProfileRefreshQueueValueActive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find Organisations with Stale Profiles] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify the SOLICITOR_PROFILE exists in the refresh queue and is not active] as in [S-013.02_VerifySolicitorProfileRefreshQueueValueIsNotActive]

  @S-013.03
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 3 - Find Organisation Changes
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find Organisation Changes] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-013.05
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 5 - Find User Changes
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find User Changes] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.