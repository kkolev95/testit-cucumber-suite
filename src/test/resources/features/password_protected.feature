Feature: Password-Protected Tests
  As a test author
  I want to password-protect my tests
  So that only invited participants can access them

  Background:
    Given a registered and authenticated author

  Scenario: Accessing a password-protected test without a password is blocked
    Given the author has created a password-protected test with password "secret123"
    When an anonymous user accesses the test
    Then the response status should be 403
    And the response indicates a password is required

  Scenario: Verifying the correct password grants access
    Given the author has created a password-protected test with password "secret123"
    When an anonymous user verifies the password "secret123"
    Then the response status should be 200

  Scenario: Verifying the wrong password is rejected
    Given the author has created a password-protected test with password "secret123"
    When an anonymous user verifies the password "wrongpassword"
    Then the response status should be 400
