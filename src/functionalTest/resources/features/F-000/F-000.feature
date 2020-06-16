@F-000 @Smoke
Feature: Access Organisation Role Mapping API

  Background:
    Given an appropriate test context as detailed in the test data source
  @S-000
  Scenario: must access Organisation Role Mapping API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Organisation Role Mapping API],
    And it is submitted to call the [Access Organisation Role Mapping API] operation of [Organisation Role Mapping API],
    Then a positive response is received,
    And the response has all other details as expected.
