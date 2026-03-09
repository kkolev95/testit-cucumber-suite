Feature: API Edge Cases
  As an API consumer
  I want invalid inputs to be rejected and valid inputs to be accepted
  So that the system enforces data integrity and supports standard formats

  Scenario Outline: Creating a test with invalid settings returns 400
    Given a registered and authenticated user
    When the user attempts to create a test where "<field>" has invalid value "<value>"
    Then the response status should be 400

    Examples:
      | field        | value              |
      | title        | (whitespace)       |
      | max_attempts | 0                  |
      | max_attempts | -1                 |
      | visibility   | invalid_visibility |

  Scenario: Creating a question with empty text returns 400
    Given a registered and authenticated user
    And the user has created a test titled "Edge Test"
    When the user creates a question with empty text
    Then the response status should be 400

  Scenario: Creating a multiple choice question with no answers returns 400
    Given a registered and authenticated user
    And the user has created a test titled "Edge Test"
    When the user creates a multiple choice question with no answers
    Then the response status should be 400

  Scenario: Creating a multiple choice question with no correct answer returns 400
    Given a registered and authenticated user
    And the user has created a test titled "Edge Test"
    When the user creates a multiple choice question with no correct answer
    Then the response status should be 400

  Scenario: Creating a question with an invalid type returns 400
    Given a registered and authenticated user
    And the user has created a test titled "Edge Test"
    When the user creates a question with an invalid type
    Then the response status should be 400

  Scenario: Accessing a non-existent test slug returns 404
    Given a registered and authenticated user
    When the user requests a test with slug "this-slug-does-not-exist-xyz-abc-123"
    Then the response status should be 404

  Scenario Outline: Valid non-standard email formats are accepted during registration
    When a new user registers with email "<email>"
    Then the response status should be 201

    Examples:
      | email                          |
      | user+tag@example.com           |
      | user.name@sub.example.com      |
      | firstname.lastname@example.org |

  Scenario: Submitting an attempt without answering any questions scores zero
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has started the test
    When the user submits without answering any questions
    Then the score should be 0
