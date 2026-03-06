Feature: Profile Management
  As an authenticated user
  I want to update my profile
  So that my account information stays current

  Background:
    Given a registered and authenticated user

  Scenario: Patching first name only preserves the last name
    When the user patches their first name to "UpdatedFirst"
    Then the response status should be 200
    And the first name in the response is "UpdatedFirst"
    And the last name in the response is "Tester"

  Scenario: Patching last name only preserves the first name
    When the user patches their last name to "UpdatedLast"
    Then the response status should be 200
    And the last name in the response is "UpdatedLast"
    And the first name in the response is "Cucumber"

  Scenario: Patching both names updates both fields
    When the user patches their name to first "NewFirst" and last "NewLast"
    Then the response status should be 200
    And the first name in the response is "NewFirst"
    And the last name in the response is "NewLast"

  Scenario: Unauthenticated profile patch is rejected
    When an unauthenticated user patches the profile first name to "Hacker"
    Then the response status should be 401
