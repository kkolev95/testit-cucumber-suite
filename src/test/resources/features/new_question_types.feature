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

  # -------------------------------------------------------------------------
  # multi_select — wrong-only selection scores zero
  # -------------------------------------------------------------------------

  Scenario: multi_select selecting only wrong answers scores zero
    Given the author has created a test with a multi select question
    And an anonymous user has started the test
    When the user submits "wrong" multi select answers
    Then the score should be 0

  # -------------------------------------------------------------------------
  # multi_select — updating correct answers affects scoring
  # -------------------------------------------------------------------------

  Scenario: Updating multi_select correct answers is reflected in the question response
    Given the author has created a test with a multi select question
    When the author updates the multi_select question to make only the first wrong answer correct
    Then the updated question has exactly 1 correct answer

  # -------------------------------------------------------------------------
  # exact_answer — updating correct answer changes scoring
  # -------------------------------------------------------------------------

  Scenario: Updating exact_answer correct answer means the old answer no longer scores
    Given the author has created a test with an exact answer question "Berlin"
    And an anonymous user has started the test
    When the author updates the exact answer question to accept "Frankfurt"
    And the user submits the text answer "Berlin"
    Then the score should be 0

  # -------------------------------------------------------------------------
  # Mixed MC + MS test — both questions scored independently
  # -------------------------------------------------------------------------

  Scenario: Mixed MC and multi_select test — all correct scores 100
    Given the author has created a test with one MC and one multi_select question
    And an anonymous user has started the test
    When the user submits all correct answers for both questions in the mixed MC+MS test
    Then the score should be 100

  # -------------------------------------------------------------------------
  # Mixed MC + EA test — both questions scored independently
  # -------------------------------------------------------------------------

  Scenario: Mixed MC and exact_answer test — all correct scores 100
    Given the author has created a test with one MC and one exact_answer question
    And an anonymous user has started the test
    When the user submits all correct answers for both questions in the mixed MC+EA test
    Then the score should be 100

  # -------------------------------------------------------------------------
  # Folder assignment edge cases
  # -------------------------------------------------------------------------

  Scenario: Assigning a test to a non-existent folder returns 400
    Given the author has created a test with a multiple choice question
    And the user has created a company
    When the user assigns the test to a non-existent folder
    Then the response status should be 400

  Scenario: Non-owner cannot assign test to a folder
    Given the author has created a test with a multiple choice question
    And the user has created a company
    And the user has created a folder named "Target Folder"
    And another user is registered and authenticated
    When the other user tries to assign the test to a folder
    Then the response status should be 404

  # -------------------------------------------------------------------------
  # Folder detail fields
  # -------------------------------------------------------------------------

  Scenario: Folder detail endpoint returns all required fields
    Given the user has created a company
    And the user has created a folder named "Detail Folder"
    When the user fetches the folder details
    Then the response status should be 200
    And the folder detail has required fields
