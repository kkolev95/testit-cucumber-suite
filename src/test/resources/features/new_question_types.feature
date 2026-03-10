Feature: New Question Types
  As a test author
  I want to create multi_select and exact_answer questions
  So that I can build richer assessments

  Background:
    Given a registered and authenticated author

  # -------------------------------------------------------------------------
  # multi_select — validation
  # -------------------------------------------------------------------------

  Scenario: multi_select question with no correct answers is rejected
    Given the user has created a test titled "MS No Correct"
    When the author creates a multi_select question with no correct answers
    Then the response status should be 400

  # -------------------------------------------------------------------------
  # multi_select — all-or-nothing scoring
  # -------------------------------------------------------------------------

  Scenario: multi_select partial correct selection scores zero
    Given the author has created a test with a multi select question
    And an anonymous user has started the test
    When the user submits "partial correct" multi select answers
    Then the score should be 0

  Scenario: multi_select correct plus wrong selection scores zero
    Given the author has created a test with a multi select question
    And an anonymous user has started the test
    When the user submits "correct plus wrong" multi select answers
    Then the score should be 0

  # -------------------------------------------------------------------------
  # exact_answer — validation
  # -------------------------------------------------------------------------

  Scenario: exact_answer with empty correct_answer is rejected
    Given the user has created a test titled "EA Empty CA"
    When the author creates an exact_answer question with correct_answer ""
    Then the response status should be 400

  Scenario: exact_answer correct_answer exceeding 30 chars is rejected
    Given the user has created a test titled "EA Too Long"
    When the author creates an exact_answer question with correct_answer of 31 chars
    Then the response status should be 400

  # -------------------------------------------------------------------------
  # exact_answer — take endpoint security
  # -------------------------------------------------------------------------

  Scenario: exact_answer take endpoint omits the correct answer
    Given the author has created a test with an exact answer question "Paris"
    When an anonymous user accesses the test
    Then the take endpoint does not reveal the correct answer

  # -------------------------------------------------------------------------
  # exact_answer — empty submission scoring
  # -------------------------------------------------------------------------

  Scenario: exact_answer empty submission scores zero
    Given the author has created a test with an exact answer question "Paris"
    And an anonymous user has started the test
    When the user submits without answering any questions
    Then the score should be 0
