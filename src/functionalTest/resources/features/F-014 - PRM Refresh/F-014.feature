@F-013
Feature: F-013 : PRM Scheduler

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-014.01
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 6 - Refresh User - Single User Mode
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh User - Single User Mode] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
