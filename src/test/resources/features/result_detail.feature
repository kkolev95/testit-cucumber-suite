Feature: Individual Result Detail
  As an author
  I want to view details of an individual test result
  So that I can review a specific submission in depth

  Scenario: Author can view an individual result detail
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author fetches the first result's detail
    Then the response status should be 200
    And the result detail has required fields

  Scenario: Non-author cannot view a result detail
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    And another user is registered and authenticated
    When the other user requests the result detail
    Then the response status should be 404
