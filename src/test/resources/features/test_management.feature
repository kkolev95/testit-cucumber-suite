Feature: Test Management
  As an authenticated user
  I want to create, list, update, and delete tests
  So that I can manage my question bank

  Background:
    Given a registered and authenticated user

  Scenario: Create a test with a title
    When the user creates a test titled "My Cucumber Test"
    Then the response status should be 201
    And the test slug is present in the response

  Scenario: Created test appears in the test list
    Given the user has created a test titled "Listed Cucumber Test"
    When the user lists their tests
    Then the response status should be 200
    And the list contains "Listed Cucumber Test"

  Scenario: Test title can be updated
    Given the user has created a test titled "Original Cucumber Title"
    When the user updates the test title to "Updated Cucumber Title"
    Then the response status should be 200
    And the test title is "Updated Cucumber Title"

  Scenario: Test can be deleted
    Given the user has created a test titled "Deletable Cucumber Test"
    When the user deletes the test
    Then the response status should be 204
