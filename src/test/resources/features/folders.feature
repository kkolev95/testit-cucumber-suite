Feature: Folder Management
  As an authenticated author
  I want to organise my tests into folders
  So that I can manage my test library efficiently

  Background:
    Given a registered and authenticated user
    And the user has created a company

  Scenario: Create a folder within a company
    When the user creates a folder named "Geography Tests"
    Then the response status should be 201
    And the folder appears in the company folder list

  Scenario: Assign a test to a folder updates the test and folder count
    Given the user has created a folder named "Science Tests"
    And the user has created a test titled "Folder Assignment Test"
    When the user assigns the test to the folder
    Then the folder test count is 1
    And the test is linked to the folder

  Scenario: Unassign a test from a folder clears the folder link
    Given the user has created a folder named "History Tests"
    And the user has created a test titled "Folder Removal Test"
    And the user has assigned the test to the folder
    When the user unassigns the test from the folder
    Then the folder test count is 0

  Scenario: Reassigning a test between folders updates both counts
    Given the user has created folders "Folder A" and "Folder B"
    And the user has created a test titled "Moving Test"
    And the user has assigned the test to "Folder A"
    When the user reassigns the test to "Folder B"
    Then "Folder A" has a test count of 0
    And "Folder B" has a test count of 1

  Scenario: Assigning to a non-existent folder returns 400
    Given the user has created a test titled "Ghost Folder Test"
    When the user assigns the test to folder id 999999
    Then the response status should be 400
