Feature: Test Analytics
  As a test author
  I want to view analytics for my tests
  So that I can understand how test takers are performing

  Background:
    Given a registered and authenticated author

  Scenario: Analytics with no submissions shows zero total attempts
    Given the author has created a test with a multiple choice question
    When the author requests analytics for the test
    Then the total attempts should be 0

  Scenario: Analytics after a submission reflects the attempt
    Given the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author requests analytics for the test
    Then the total attempts should be 1
    And the analytics include question stats

  Scenario: Analytics is not accessible without authentication
    Given the author has created a test with a multiple choice question
    When an unauthenticated user requests analytics for the test
    Then the response status should be 401

  Scenario: Analytics is not accessible by a different user
    Given the author has created a test with a multiple choice question
    When a different authenticated user requests analytics for the test
    Then access to the analytics is denied
