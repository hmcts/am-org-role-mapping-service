@F-020
Feature: F-020 : IRM Scheduler

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-020.01
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM scheduler process empty judicial queue
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Process empty IRM Judicial Queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.02
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM invite user
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Invite IDAM user] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.03
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM add user1 to queue
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Add IDAM user to queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.04
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM add user2 to queue
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Add IDAM user to queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.05
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM update user
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Update IDAM user] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.06
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM scheduler process populated judicial queue
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    And a successful call [to make the IRM queue value active] as in [S-020.06_MakeIrmQueueValue1Active]
    And a successful call [to make the IRM queue value active] as in [S-020.06_MakeIrmQueueValue2Active]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Process populated IRM Judicial Queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.07a
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM delete user1 from queue
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete IDAM user from queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.07b
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM delete user2 from queue
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete IDAM user from queue] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected

  @S-020.08
  @FeatureToggle(EV:IRM_FTA_ENABLED=on)
  Scenario: trigger IRM scheduler delete inactive queue entries
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to make the IRM queue value inactive] as in [F-020-MakeAllIrmQueueValuesInactive]
    When a request is prepared with appropriate values,
    And it is submitted to call the [Delete inactive queue entries] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected