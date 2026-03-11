Feature: Security
  As a security-conscious API consumer
  I want to ensure resources are properly protected
  So that users cannot access or modify each other's data

  Scenario: User B cannot read User A's test
    Given a registered and authenticated user
    And the user has created a test titled "Private Test"
    And another user is registered and authenticated
    When the other user requests the test
    Then the response status should be 404

  Scenario: User B cannot update User A's test
    Given a registered and authenticated user
    And the user has created a test titled "Private Test"
    And another user is registered and authenticated
    When the other user tries to update the test
    Then the response status should be 404

  Scenario: User B cannot delete User A's test
    Given a registered and authenticated user
    And the user has created a test titled "Private Test"
    And another user is registered and authenticated
    When the other user tries to delete the test
    Then the response status should be 404

  Scenario: User B cannot read User A's results
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    And another user is registered and authenticated
    When the other user requests the test results
    Then the response status should be 404

  Scenario: Request with an invalid token returns 401
    Given a registered and authenticated user
    When a request is made with an invalid token
    Then the response status should be 401

  Scenario: Request with a malformed token returns 401
    Given a registered and authenticated user
    When a request is made with a malformed token
    Then the response status should be 401

  Scenario: SQL injection in test title is handled safely
    Given a registered and authenticated user
    When the user creates a test titled "' OR '1'='1"
    Then the response status should not be 500

  Scenario: XSS payload in test title is handled safely
    Given a registered and authenticated user
    When the user creates a test titled "<img src=x onerror=alert(1)>"
    Then the response status should not be 500

  Scenario: Oversized test title is rejected
    Given a registered and authenticated user
    When the user creates a test with an oversized title
    Then the response status should be 400

  Scenario: Extremely long email address is rejected during registration
    When a new user registers with email "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com"
    Then the response status should be 400
