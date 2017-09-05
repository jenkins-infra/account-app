Feature: Provide special admin-only features

  Background: An administrator
    Given that I am an administrator
    When I attempt to login
    And I view the admin panel

  Scenario: Manage circuit breaker
  Scenario: Look up existing user record
  Scenario: Delete user
  Scenario: Reset existing user's password
  Scenario: Reset existing user's email
