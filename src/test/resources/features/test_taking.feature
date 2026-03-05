Feature: Test Taking
  As an anonymous user
  I want to take and submit tests
  So that I can be assessed

  Background:
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question

  Scenario: Anonymous user can access a published test
    When an anonymous user accesses the test
    Then the response status should be 200
    And the questions are returned without revealing correct answers

  Scenario: Submitting all correct answers scores 100 percent
    Given an anonymous user has started the test
    When the user submits all correct answers
    Then the score should be 100

  Scenario: Submitting all wrong answers scores 0 percent
    Given an anonymous user has started the test
    When the user submits all wrong answers
    Then the score should be 0

  Scenario: Author can view submission results
    Given an anonymous user has completed the test
    When the author views the test results
    Then the response status should be 200
    And the results list is not empty

  Scenario: Cannot submit an already submitted attempt
    Given an anonymous user has submitted the test
    When the user tries to submit the same attempt again
    Then the response status should be 400
