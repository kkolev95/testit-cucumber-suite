Feature: Question Management
  As an authenticated author
  I want to add, update, delete, and reorder questions in my tests
  So that I can build comprehensive assessments

  Background:
    Given a registered and authenticated user
    And the user has created a test titled "Question Management Test"

  Scenario: Add a multiple choice question
    When the author adds a multiple choice question
    Then the response status should be 201
    And the question id is present in the response

  Scenario: Add an exact answer question
    When the author adds an exact answer question with correct answer "Paris"
    Then the response status should be 201
    And the question id is present in the response

  Scenario: Add a multi select question with multiple correct answers
    When the author adds a multi select question
    Then the response status should be 201
    And the question id is present in the response

  Scenario: Update a question text
    Given the author has added a multiple choice question
    When the author updates the question text to "Updated question text"
    Then the response status should be 200
    And the question text is "Updated question text"

  Scenario: Delete a question removes it from the test
    Given the author has added a multiple choice question
    When the author deletes the question
    Then the question no longer exists in the test

  Scenario: Reorder questions changes their order
    Given the author has added 3 questions to the test
    When the author reorders the questions in reverse
    Then the questions appear in reverse order

  Scenario: Updating a non-existent question returns 404
    When the author updates a non-existent question
    Then the response status should be 404
