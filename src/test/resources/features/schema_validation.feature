Feature: API Response Schema Validation
  As an API consumer
  I want key responses to have consistent and safe structure
  So that my code can reliably parse them and sensitive data is never exposed

  Scenario: Login response contains tokens and no password field
    Given a registered user exists
    When the user logs in with correct credentials
    Then the response contains an access token
    And the response contains a refresh token
    And the response does not contain a password field

  Scenario: Profile response contains required fields and no password field
    Given a registered and authenticated user
    When the user requests their profile
    Then the profile response has required fields
    And the profile response does not contain a password field

  Scenario: Test creation response contains required fields
    Given a registered and authenticated user
    When the user creates a test titled "Schema Check"
    Then the test response has required fields

  Scenario: Attempt creation response contains required fields
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    When an anonymous user starts an attempt
    Then the attempt response has required fields

  Scenario: Result response items contain required fields
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author views the test results
    Then the result response items have required fields

  Scenario: Analytics response contains required fields
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    When the author requests analytics for the test
    Then the analytics response has required fields

  Scenario: Test slug matches the URL-safe lowercase pattern
    Given a registered and authenticated user
    When the user creates a test titled "Slug Format Check"
    Then the test slug matches the expected format

  Scenario: Attempt started_at is a valid ISO 8601 timestamp
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    When an anonymous user starts an attempt
    Then the attempt started_at is a valid ISO 8601 timestamp

  Scenario: Result submitted_at is a valid ISO 8601 timestamp
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author views the test results
    Then the result submitted_at is a valid ISO 8601 timestamp

  Scenario: Result score is a number within the 0 to 100 range
    Given a registered and authenticated author
    And the author has created a test with a multiple choice question
    And an anonymous user has submitted the test
    When the author views the test results
    Then the result score is within valid range

  Scenario: Test visibility field contains a valid enum value
    Given a registered and authenticated user
    When the user creates a test titled "Visibility Enum Check"
    Then the test visibility is a valid enum value
