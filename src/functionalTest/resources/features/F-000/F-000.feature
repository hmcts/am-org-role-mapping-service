@F-000
Feature: Access Organisation Role Mapping API

  Background:
    Given an appropriate test context as detailed in the test data source
  @S-000 @FeatureToggle(orm-base-flag)
  Scenario: must access Organisation Role Mapping API
    Given a user with [an active caseworker profile],

  @S-120
  @FeatureToggle(LD:get-ld-flag=on)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-110
  @FeatureToggle(LD:get-ld-flag=off)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-111
  @FeatureToggle(IAC:iac_1_0=on)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-112
  @FeatureToggle(IAC:iac_1_0=on) @FeatureToggle(IAC:iac_1_1=off)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-113
  @FeatureToggle(IAC:iac_1_0=on) @FeatureToggle(IAC:iac_1_1=on) @FeatureToggle(LD:get-ld-flag=on)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],

