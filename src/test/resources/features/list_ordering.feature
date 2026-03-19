Feature: List Ordering and Completeness
  As an API consumer
  I want list endpoints to return items in a predictable order
  So that I can rely on consistent ordering and completeness guarantees

  Background:
    Given a registered and authenticated author

  Scenario: Tests are returned newest first
    Given the author has created 3 tests in sequence
    When the author lists their tests
    Then the first test in the list is the most recently created
    And the list contains at least 3 tests

  Scenario: All created tests appear in the list
    Given the author has created 3 tests in sequence
    When the author lists their tests
    Then all 3 created tests appear in the list

  Scenario: Pagination parameters have no effect — full list is always returned
    Given the author has created 3 tests in sequence
    When the author lists their tests with a limit of 1
    Then all 3 created tests appear in the list

  Scenario: Results are returned newest submitted first
    Given the author has created a test with a multiple choice question
    And 3 anonymous users have submitted the test in sequence
    When the author views the test results
    Then the results are ordered newest submitted first
