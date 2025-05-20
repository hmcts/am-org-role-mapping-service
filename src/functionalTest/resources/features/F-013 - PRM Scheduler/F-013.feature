@F-013
Feature: F-013 : PRM Scheduler

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-013.04
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: trigger PRM scheduler process 4 - Find Users With Stale Organisations
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the ORGANISATION refresh queue value active] as in [S-013.04_MakeOrganisationRefreshQueueValueActive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Find Users with Stale Organisations] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify the ORGANISATION exists in the refresh queue and is not active] as in [S-013.04_VerifyOrganisationRefreshQueueValueIsNotActive]
