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

  Scenario: Average score reflects two submissions with different results
    Given the author has created a test with a multiple choice question
    And one anonymous user submits correctly and another submits with no answers
    When the author requests analytics for the test
    Then the average score is approximately 50 percent

  Scenario: Completion rate reflects an abandoned and a submitted attempt
    Given the author has created a test with a multiple choice question
    And one anonymous user abandons the test and another submits
    When the author requests analytics for the test
    Then the completion rate is approximately 50 percent

  Scenario: Answer distribution in question stats reflects each answer selected
    Given the author has created a test with a multiple choice question
    And one anonymous user selects the correct answer and another selects the wrong answer
    When the author requests analytics for the test
    Then the question stats show 2 answered and 1 correct

  Scenario: Question difficulty is a normalised score within valid range
    Given the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author requests analytics for the test
    Then the question difficulty is within range 0 to 1

  Scenario: Pass rate is a valid percentage after at least one submission
    Given the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author requests analytics for the test
    Then the pass rate is within valid range
