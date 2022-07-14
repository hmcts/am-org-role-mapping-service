@F-000
Feature:F-000 Access Organisation Role Mapping API

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


