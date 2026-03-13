Feature: API Contracts
  As an API consumer
  I want consistent structural guarantees from the API
  So that client code can rely on predictable responses

  Background:
    Given a registered and authenticated user

  Scenario: New user's test list is an empty JSON array
    When the user lists their tests
    Then the response is an empty JSON array

  Scenario: All created tests appear in the list
    Given the user has created a test titled "Contract Test A"
    And the user has created a test titled "Contract Test B"
    When the user lists their tests
    Then the list contains exactly 2 items

  Scenario: Deleting a test removes it from the list
    Given the user has created a test titled "Keep Me"
    And the user has created a test titled "Delete Me"
    When the user deletes the test
    And the user lists their tests
    Then the list contains exactly 1 items

  Scenario: API responses include a JSON Content-Type header
    When the user lists their tests
    Then the response Content-Type is application/json

  Scenario: Fetching a question by id returns its text and answers
    Given the user has created a test titled "Question Detail Test"
    And the author has added a multiple choice question
    When the user fetches the question details
    Then the response status should be 200
    And the question details contain the question text and answers

  Scenario: Fetching a non-existent question returns 404
    Given the user has created a test titled "Question Detail Test"
    When the user fetches a non-existent question
    Then the response status should be 404
