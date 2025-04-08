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

  @S-013.02
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 2 - Find Organisations With Stale Profiles
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find Organisations With Stale Profiles] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
