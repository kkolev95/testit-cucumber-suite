Feature: Company Invites
  As a company admin
  I want to invite users to my company
  So that they can collaborate on tests

  Background:
    Given a registered and authenticated user
    And the user has created a company named "Invite Corp"
    And a second user has registered

  Scenario: Admin can invite a user to the company
    When the admin sends an invite to the second user as "student"
    Then the response status should be 201
    And the invite token is returned

  Scenario: Duplicate pending invite is rejected
    Given the admin has sent an invite to the second user as "student"
    When the admin sends an invite to the second user as "instructor"
    Then the response status should be 400

  Scenario: Pending invites appear in the invite list
    Given the admin has sent an invite to the second user as "student"
    When the admin lists the company invites
    Then the second user's email appears in the invite list

  Scenario: Accepting an invite adds the user to the company
    Given the admin has sent an invite to the second user as "student"
    And the second user has logged in
    When the second user accepts the invite
    Then the company member count is 2
