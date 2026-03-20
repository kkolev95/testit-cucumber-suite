Feature: Cascading Delete Behavior
  As a test author
  I want deleting companies and folders to correctly cascade to related resources
  So that data is cleaned up predictably

  Background:
    Given a registered and authenticated user

  Scenario: Deleting a company cascade-deletes all its company tests
    Given the author has set up a company with a company test
    When the user deletes the company
    Then the company test is not accessible via the company endpoint
    And the company test is not accessible via the personal test endpoint

  Scenario: Deleting a company does not affect the author's personal tests
    Given the author has set up a personal test and an unrelated company
    When the user deletes the company
    Then the personal test is still accessible

  Scenario: Deleting a folder unlinks the assigned test and does not delete it
    Given the author has set up a personal test assigned to a folder
    When the user deletes the folder
    Then the test's folder field is null
    And the personal test is still accessible
