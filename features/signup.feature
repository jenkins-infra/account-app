Feature: New user sign up

  Scenario: New user sign up
    Given that I am unauthenticated
    And I do not have an existing user
    When I sign up
    Then I should be able to login
