Feature: Company Management
  As an authenticated user
  I want to manage companies
  So that I can organise tests under a company umbrella

  Background:
    Given a registered and authenticated user

  Scenario: Create a company with a valid name
    When the user creates a company named "Acme Corp"
    Then the response status should be 201
    And the company is returned with a positive id

  Scenario: Company appears in the listing after creation
    When the user creates a company named "Listed Corp"
    Then the company appears in the user's company list

  Scenario: Update a company's name
    Given the user has created a company named "Old Corp"
    When the user updates the company name to "New Corp"
    Then the response status should be 200
    And the company name is "New Corp"

  Scenario: Delete a company
    Given the user has created a company named "Delete Corp"
    When the user deletes the company
    Then the response status should be 204
    And the company no longer appears in the list

  Scenario: Creating a company when unauthenticated returns 401
    When an unauthenticated user creates a company named "Ghost Corp"
    Then the response status should be 401

  Scenario: Creator is listed as admin member of a new company
    Given the user has created a company named "Member Corp"
    When the user lists the company members
    Then the member list contains the user with role "admin"

  Scenario: Create a test under a company
    Given the user has created a company named "Test Corp"
    When the user creates a company test titled "Internal Quiz"
    Then the response status should be 201
    And the test appears in the company test list

  Scenario: Admin can fetch a company test by slug
    Given the user has created a company named "CRUD Corp"
    And the user has created a company test titled "Company Quiz"
    When the user fetches the company test by slug
    Then the response status should be 200

  Scenario: Admin can update a company test title
    Given the user has created a company named "CRUD Corp"
    And the user has created a company test titled "Company Quiz"
    When the user updates the company test title to "Updated Company Quiz"
    Then the response status should be 200

  Scenario: Admin can delete a company test
    Given the user has created a company named "CRUD Corp"
    And the user has created a company test titled "To Delete Quiz"
    When the user deletes the company test
    Then the response status should be 204

  Scenario: Non-member cannot access a company test
    Given the user has created a company named "Private Corp"
    And the user has created a company test titled "Secret Quiz"
    When a non-member requests the company test
    Then the response status should be 401

  Scenario: Admin can change a member's role
    Given the user has created a company named "Roles Corp"
    And the admin has invited a second user who joined as "student"
    When the admin changes the second member's role to "admin"
    Then the response status should be 200

  Scenario: Non-admin member cannot change roles
    Given the user has created a company named "Roles Corp"
    And the admin has invited a second user who joined as "student"
    When a non-admin member tries to update a member's role
    Then the response status should be 403

  Scenario: Admin can remove a member from the company
    Given the user has created a company named "Remove Corp"
    And the admin has invited a second user who joined as "student"
    When the admin removes the second member from the company
    Then the response status should be 204

  Scenario: Last admin cannot remove themselves from the company
    Given the user has created a company named "Last Admin Corp"
    When the admin tries to remove themselves from the company
    Then the response status should be 400
