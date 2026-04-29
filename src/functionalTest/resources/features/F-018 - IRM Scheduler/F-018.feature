@F-018
Feature: F-018 : IRM Scheduler

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-018.01
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM scheduler process judicial queue
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [S-018.01_MakeIrmQueueValueInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Process IRM Judicial Queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-018.02
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM invite user
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [S-018.02_MakeIrmQueueValueInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Invite IDAM user] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected