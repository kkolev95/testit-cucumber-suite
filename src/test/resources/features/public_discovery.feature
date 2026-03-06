Feature: Public Test Discovery
  As anyone browsing the platform
  I want to discover publicly available tests
  So that I can find tests without needing a direct link

  Scenario: Public test list is accessible without authentication
    When an anonymous user requests the public test list
    Then the response status should be 200

  Scenario: A public test appears in the public list
    Given a registered and authenticated author
    And the author has created a test with visibility "public"
    When an anonymous user requests the public test list
    Then the test appears in the public list

  Scenario: A link_only test does not appear in the public list
    Given a registered and authenticated author
    And the author has created a test with visibility "link_only"
    When an anonymous user requests the public test list
    Then the test does not appear in the public list
