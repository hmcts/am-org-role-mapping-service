@F-014
Feature: F-014 : Refresh Professional User

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-014.00
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: 00-Setup Professional Organisation & User Data
    #This scenario creates everything needed for subsequent tests
    Given a user with [Prd admin access],
    And a successful call is made [to create an organisation in professional reference data] as in [S-014.00__CreateProfessionalOrganisation],
    And a successful call is made [to update the organisation in professional reference data to active] as in [S-014.00__UpdateProfessionalOrganisation].
    #And a successful call is made [to create a user in professional reference data] as in [S-014.00__CreateProfessionalUser],
    #And a successful call is made [to create user configured access for professional user] as in [S-014.00__CreateUserConfiguredAccess],

  # P1. All flags = true => expected 2 roles generated
  @S-014.01
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: successful refresh of professional user - single user - All flags true and PRD enabled true
    Given a user with [Professional admin access],
    #And a successful call [to update User Configured Access set it to enabled] as in [S-014.01__UpdateUserConfiguredAccess],
    And a successful call [to verify professional user has userAccessTypes enabled] as in [S-014.01__VerifyProfessionalUser],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Professional User Role Assignments] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify role assignments are correct for test userId] as in [S-014.01__VerifyRoleAssignments],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments].


  # N1 User not found - 0 roles generated
  @S-014.02
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: unsuccessful refresh of professional user - single user
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Professional User Role Assignments] operation of [Organisation Role Mapping],
    Then a negative response is received,
    And the response has all other details as expected.


  # P2 CCD 3 flags = true (but PRD enabled = false) => expected 2 roles generated
  @S-014.03
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: successful refresh of professional user - single user - All flags true and PRD enabled false
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify professional user has userAccessTypes disabled] as in [S-014.03__VerifyProfessionalUser],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Professional User Role Assignments] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify role assignments are correct for test userId] as in [S-014.03__VerifyRoleAssignments],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments].


  # N2 User found without PRD access types - 0 roles generated positive scenario 200 returned
  @S-014.04
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: unsuccessful refresh of professional user - single user no PRD
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to verify professional user has userAccessTypes disabled] as in [S-014.04__VerifyProfessionalUser],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments],
    When a request is prepared with appropriate values,
    And it is submitted to call the [Refresh Professional User Role Assignments] operation of [Organisation Role Mapping],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to verify role assignments are correct for test userId] as in [S-014.04__VerifyRoleAssignments],
    And a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments].


  @S-014.5
  @FeatureToggle(EV:PRM_FTA_ENABLED=on)
  Scenario: Delete and Clean Up Professional Organisation test data
   # Given a successful call [to delete existing role assignments corresponding to the test userId] as in [DeleteDataForProfessionalRoleAssignments]
   # And a successful call [to delete organisation created as test data] as in [S-014.00__DeleteDataForProfessionalOrganisation]