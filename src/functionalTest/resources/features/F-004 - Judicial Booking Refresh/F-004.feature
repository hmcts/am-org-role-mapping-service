@F-004 @FeatureToggle(EV:JUDICIAL_FTA_ENABLED=on)
Feature: F-004 : Judicial Refresh Request (with/without Bookings)

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-031
  @FeatureToggle(EV:BOOKING_FTA_ENABLED=on)
  Scenario: must successfully create judicial role assignments for a user having single judicial booking
    Given a user with [a judicial profile that supports judicial bookings],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [F-004_DeleteDataForBookings],
    And a successful call [to snapshot judicial details] as in [S-031__SnapshotJudicialDetails],
    And a successful call [to create single booking for this user] as in [F-004_CreateDataForBookings],
    And a successful call [to refresh a judicial user's role assignments] as in [F-004_PostRefreshRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user having existing judicial role assignments],
    And the request [contains the actorId of the user for which above booking is created],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response [contains a bookable fee-paid-judge role-assignment],
    And the response [contains one booked judge/fee-paid role-assignment],
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments].

  @S-032
  @FeatureToggle(EV:BOOKING_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mappings for User having multiple Judicial bookings
    Given a user with [a judicial profile that supports judicial bookings],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [F-004_DeleteDataForBookings],
    And a successful call [to snapshot judicial details] as in [S-032__SnapshotJudicialDetails],
    And a successful call [to create single booking for this user] as in [F-004_CreateDataForBookings],
    And a successful call [to create extra booking for this user] as in [S-032_CreateDataForNewBookings],
    And a successful call [to refresh a judicial user's role assignments] as in [F-004_PostRefreshRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user having existing judicial role assignments],
    And the request [contains the actorId of the user for which above bookings are created],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response [contains a bookable fee-paid-judge role-assignment],
    And the response [contains multiple booked judge/fee-paid role-assignments],
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments].

  @S-033
  @FeatureToggle(EV:BOOKING_FTA_ENABLED=on)
  Scenario: must successfully create judicial role mappings for User having no judicial booking
    Given a user with [a judicial profile that supports judicial bookings],
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments]
    And a successful call [to delete existing bookings corresponding to the test actorId] as in [F-004_DeleteDataForBookings],
    And a successful call [to snapshot judicial details] as in [S-033__SnapshotJudicialDetails],
    And a successful call [to refresh a judicial user's role assignments] as in [F-004_PostRefreshRequestToORM],
    When a request is prepared with appropriate values,
    And the request [contains the actorId of the user having existing judicial role assignments],
    And the request [contains the actorId of the user for which no bookings exist],
    And it is submitted to call the [Fetch Assignment From Role Assignment Service] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response [contains a bookable fee-paid-judge role-assignment],
    And the response [contains no booked judge/fee-paid role-assignments],
    And the response has all other details as expected,
    And a successful call [to delete existing role assignments corresponding to the test actorId] as in [F-004_DeleteDataForRoleAssignments].
