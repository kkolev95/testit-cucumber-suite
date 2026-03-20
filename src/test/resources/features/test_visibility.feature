Feature: Test Visibility Modes
  As a test author
  I want to control who can access my tests
  So that I can manage test distribution

  Background:
    Given a registered and authenticated author

  Scenario Outline: Visibility mode controls anonymous access to the test
    Given the author has created a test with visibility "<visibility>"
    When an anonymous user accesses the test
    Then the response status should be <expected_status>

    Examples:
      | visibility         | expected_status |
      | public             | 200             |
      | link_only          | 200             |
      | password_protected | 403             |

  Scenario: Test visibility can be updated from link_only to public
    Given the author has created a test with visibility "link_only"
    When the author changes the test visibility to "public"
    Then the response status should be 200
    And an anonymous user can access the test

  Scenario: Changing visibility from link_only to public makes the test discoverable in the public list
    Given the author has created a test with visibility "link_only"
    When the author changes the test visibility to "public"
    And an anonymous user requests the public test list
    Then the test appears in the public list

  Scenario: Changing visibility from public to link_only removes the test from the public list
    Given the author has created a test with visibility "public"
    When the author changes the test visibility to "link_only"
    And an anonymous user requests the public test list
    Then the test does not appear in the public list
