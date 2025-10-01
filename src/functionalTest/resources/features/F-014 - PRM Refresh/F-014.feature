@F-014
Feature: F-014 : Refresh Professional User

  Background:
    Given an appropriate test context as detailed in the test data source


  # P1. All flags = true => expected 2 roles generated
  @S-014.01
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: successful refresh of professional user - single user -PRD enabled
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify professional user] as in [S-014.01__VerifyProfessionalUser],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh User - Single User Mode] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.



  # N1 User not found - 0 roles generated
  @S-014.02
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: unsuccessful refresh of professional user - single user
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Invalid User - Single User Mode] operation of [Organisation Role Mapping],
    Then a negative response is received,
    And the response has all other details as expected.



  # N2 User found without PRD access types - 0 roles generated
  @S-014.04
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: unsuccessful refresh of professional user - single user no PRD
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Invalid User - Single User Mode No PRD] operation of [Organisation Role Mapping],
    Then a negative response is received,
    And the response has all other details as expected.

    # P2 CCD 3 flags = true (but PRD enabled = false) => expected 2 roles generated
  @S-014.03
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: successful refresh of professional user - single user no PRD enabled
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify professional user] as in [S-014.03__VerifyProfessionalUser],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh User - Single User Mode No PRD] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.

    


