Feature: Data Integrity
  As an API consumer
  I want scores to be calculated accurately and data to remain consistent
  So that test results are reliable and trustworthy

  Scenario: Half correct answers produces a 50 percent score
    Given a registered and authenticated author
    And the author has created a test with 2 multiple choice questions
    And an anonymous user has started the test
    When the user answers only the first question correctly and submits
    Then the score should be 50

  Scenario: No answers submitted produces a zero score
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has started the test
    When the user submits without answering any questions
    Then the score should be 0

  Scenario: Score is consistent across multiple result fetches
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has completed the test
    Then the score is consistent across 3 fetches

  Scenario: Updating a question does not change an already-submitted score
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author updates the question text to "Updated question text"
    Then the previously submitted score is unchanged

  Scenario: Multiple submissions from different takers are all recorded
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And 3 anonymous users have submitted the test
    When the author views the test results
    Then the results list has 3 entries

  Scenario: Updating a submitted attempt draft is rejected
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the user tries to update the submitted draft
    Then the response status should not be 200
