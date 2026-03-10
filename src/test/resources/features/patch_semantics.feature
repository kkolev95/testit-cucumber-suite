Feature: PATCH Field Isolation
  As an API consumer
  I want partial updates to affect only the specified fields
  So that unrelated fields are not silently reset

  Background:
    Given a registered and authenticated user

  Scenario: PATCH title only does not change visibility
    Given the user has created a test titled "Visibility Preserved Test"
    When the user patches only the title to "New Patched Title"
    Then the response status should be 200
    And the test visibility is still "link_only"

  Scenario: PATCH visibility only does not change the title
    Given the user has created a test titled "Title Preserved Test"
    When the user patches only the visibility to "public"
    Then the response status should be 200
    And the test title still contains "Title Preserved Test"

  Scenario: PATCH with an empty body keeps all fields unchanged
    Given the user has created a test titled "Stable Test"
    When the user sends an empty PATCH to the test
    Then the response status should be 200
    And the test title still contains "Stable Test"
