@F-014
Feature: F-014 : Refresh Professional User

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-014.03
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: successful refresh of professional users - batch
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh User - Batch Mode] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
