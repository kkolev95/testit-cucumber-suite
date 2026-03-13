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

  Scenario: Unauthenticated PATCH to a test is rejected
    Given the user has created a test titled "Auth Required Test"
    When an unauthenticated user patches the test title to "Hacked"
    Then the response status should be 401

  Scenario: Non-owner cannot PATCH a test
    Given the user has created a test titled "Owner Only Test"
    And another user is registered and authenticated
    When the other user patches the test title to "Hijacked"
    Then the response status should be 404

  Scenario: PATCH question text only preserves answer count
    Given the user has created a test titled "Question PATCH Test"
    And the author has added a multiple choice question
    When the user patches the question text to "Updated question text?"
    Then the response status should be 200
    And the question answer count is 3

  Scenario: Non-owner cannot PATCH a question
    Given the user has created a test titled "Question Owner Test"
    And the author has added a multiple choice question
    And another user is registered and authenticated
    When the other user patches the question text
    Then the response status should be 404

  Scenario: Admin can PATCH the company name
    Given the user has created a company named "Patch Corp"
    When the user patches the company name to "Updated Corp"
    Then the response status should be 200
    And the company name is "Updated Corp"

  Scenario: Non-member cannot PATCH the company
    Given the user has created a company named "Members Only Corp"
    And another user is registered and authenticated
    When the other user tries to patch the company name
    Then the response status should be 404

  Scenario: Admin can PATCH a folder name
    Given the user has created a company
    And the user has created a folder named "Old Folder Name"
    When the user patches the folder name to "New Folder Name"
    Then the response status should be 200
    And the folder name is "New Folder Name"

  Scenario: Admin can move a folder to be under a parent folder
    Given the user has created a company
    And the user has created a folder named "Parent Folder"
    And the user has created a folder named "Child Folder"
    When the user patches "Child Folder" to be a child of "Parent Folder"
    Then the response status should be 200
    And the folder parent field is the id of "Parent Folder"
