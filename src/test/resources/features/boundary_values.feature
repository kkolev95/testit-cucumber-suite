Feature: Boundary Values
  As an API consumer
  I want numeric and string limits to be enforced precisely
  So that values at exact boundaries behave as expected

  Scenario: max_attempts of 1 is a valid setting
    Given a registered and authenticated user
    When the user creates a test with max_attempts of 1
    Then the response status should be 201

  Scenario: max_attempts of 100 is accepted
    Given a registered and authenticated user
    When the user creates a test with max_attempts of 100
    Then the response status should be 201

  Scenario: max_attempts of 1 is enforced — second attempt by same session is rejected
    Given a registered and authenticated author
    And the author has created a test with max_attempts of 1
    And an anonymous user has submitted the test
    When the same anonymous user tries to start a new attempt
    Then starting the attempt is rejected

  Scenario: Single-character title is accepted
    Given a registered and authenticated user
    When the user creates a test titled "X"
    Then the response status should be 201
