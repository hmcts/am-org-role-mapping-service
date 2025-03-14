@F-000
Feature: F-000 : Access Organisation Role Mapping API

  Background:
    Given an appropriate test context as detailed in the test data source


  @S-000
  Scenario: must access Organisation Role Mapping API
    Given a user with [an active caseworker profile],


  @S-110
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-111
  @FeatureToggle(DB:iac_1_1=on)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-112
  @FeatureToggle(DB:iac_1_1=off)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-201
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=on)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],


  @S-202
  @FeatureToggle(EV:AZURE_SERVICE_BUS_FTA_ENABLED=off)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],

