Feature: Time Limit Behavior
  As a test author
  I want to set a time limit on my tests
  So that takers receive the time constraint information and clients can enforce it

  Background:
    Given a registered and authenticated author

  Scenario: Take endpoint exposes time_limit_minutes for a timed test
    Given the author has created a timed test with 30 minutes
    When an anonymous user accesses the test
    Then the response status should be 200
    And the take response includes time_limit_minutes of 30

  Scenario: Take endpoint returns null time_limit_minutes for an untimed test
    Given the author has created a test with no time limit
    When an anonymous user accesses the test
    Then the response status should be 200
    And the take response has null time_limit_minutes

  Scenario: Starting an attempt on a timed test returns started_at for client-side countdown
    Given the author has created a timed test with 30 minutes
    When an anonymous user starts an attempt
    Then the response status should be 201
    And the attempt response includes a started_at timestamp

  Scenario: Submitting after the time limit has elapsed is accepted
    Given the author has created a timed test with 1 minute and a question
    And an anonymous user has started the timed test
    When the time limit elapses
    And the user submits the timed attempt without answers
    Then the response status should be 200
    And the response body contains a submission success message

  Scenario: Saving a draft after the time limit has elapsed is accepted
    Given the author has created a timed test with 1 minute and a question
    And an anonymous user has started the timed test
    When the time limit elapses
    And the user saves a draft for the timed attempt
    Then the response status should be 200
