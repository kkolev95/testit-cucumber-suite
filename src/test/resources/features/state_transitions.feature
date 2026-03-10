Feature: State Transitions
  As an API consumer
  I want resource state changes to be consistently reflected
  So that deleted resources are inaccessible and data lifecycle is predictable

  Scenario: Deleted test is inaccessible via GET
    Given a registered and authenticated user
    And the user has created a test titled "Transient Test"
    And the user has deleted the created test
    When the user fetches the now-deleted test
    Then the response status should be 404

  Scenario: Starting an attempt on a deleted test is rejected
    Given a registered and authenticated user
    And the user has created a test titled "Deleted Before Attempt"
    And the user has deleted the created test
    When the user tries to start an attempt on the deleted test
    Then the response status should be 404

  Scenario: Results endpoint is inaccessible after test deletion
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    And the user has deleted the created test
    When the user requests results of the deleted test
    Then the response status should be 404

  Scenario: Deleted test no longer appears in the author's test list
    Given a registered and authenticated user
    And the user has created a test titled "Listed Then Deleted"
    And the user has deleted the created test
    When the user lists their tests
    Then the deleted test is not in the list
