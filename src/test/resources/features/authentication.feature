Feature: User Authentication
  As a user of the ExamPractices platform
  I want to register and authenticate
  So that I can create and manage tests

  Scenario: Successful registration creates a new account
    When a new user registers with valid credentials
    Then the response status should be 201

  Scenario: Login with valid credentials returns authentication tokens
    Given a registered user exists
    When the user logs in with correct credentials
    Then the response status should be 200
    And the response contains an access token

  Scenario: Login with wrong password is rejected
    Given a registered user exists
    When the user logs in with the wrong password
    Then the response status should be 401

  Scenario: Accessing profile without a token is rejected
    When the profile endpoint is requested without authentication
    Then the response status should be 401

  Scenario: Authenticated user can view their own profile
    Given a registered and authenticated user
    When the user requests their profile
    Then the response status should be 200
    And the profile email matches the registered email

  Scenario Outline: Registration is rejected with invalid data
    When a user tries to register with email "<email>" password "<password>" and confirm "<confirm>"
    Then the response status should be 400

    Examples:
      | email           | password     | confirm      |
      | not-an-email    | Password123! | Password123! |
      | valid@test.com  | abc          | abc          |
      |                 | Password123! | Password123! |
      | valid@test.com  | Password123! | WrongMatch1! |
