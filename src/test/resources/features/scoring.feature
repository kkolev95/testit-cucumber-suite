Feature: Answer Type Scoring
  As a test taker
  I want to submit answers in different formats
  So that all question types are scored correctly

  Background:
    Given a registered and authenticated author

  Scenario Outline: Exact answer question scores based on text match
    Given the author has created a test with an exact answer question "Paris"
    And an anonymous user has started the test
    When the user submits the text answer "<submitted_answer>"
    Then the score should be <expected_score>

    Examples:
      | submitted_answer | expected_score |
      | Paris            | 100            |
      | paris            | 100            |
      | London           | 0              |

  Scenario Outline: Multi select question scores based on answer selection
    Given the author has created a test with a multi select question
    And an anonymous user has started the test
    When the user submits "<selection>" multi select answers
    Then the score should be <expected_score>

    Examples:
      | selection   | expected_score |
      | all correct | 100            |
      | only wrong  | 0              |
