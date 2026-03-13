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

  Scenario Outline: Submitting answers produces the expected score
    Given an anonymous user has started the test
    When the user submits all "<answer_type>" answers
    Then the score should be <expected_score>

    Examples:
      | answer_type | expected_score |
      | correct     | 100            |
      | wrong       | 0              |

  Scenario: Author can view submission results
    Given an anonymous user has completed the test
    When the author views the test results
    Then the response status should be 200
    And the results list is not empty

  Scenario: Cannot submit an already submitted attempt
    Given an anonymous user has submitted the test
    When the user tries to submit the same attempt again
    Then the response status should be 400

  Scenario: Starting an attempt returns a positive attempt id
    When an anonymous user starts an attempt
    Then the response status should be 201
    And the attempt id is positive

  Scenario: Saving a draft with valid answers is accepted
    Given an anonymous user has started the test
    When the user saves a draft with all correct answers
    Then the response status should be 200

  Scenario: show_answers_after enabled — take endpoint remains accessible after submission
    Given the author has created a test with show_answers_after enabled
    And an anonymous user has submitted the test
    When an anonymous user accesses the test
    Then the response status should be 200
