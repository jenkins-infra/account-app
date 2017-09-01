Feature: Log into the account app


  Scenario: Navigating to the app
    Given that I am unauthenticated
    When I navigate to the home page
    Then I should see a login screen

  Scenario: Existing user login

  Scenario: Non-existing user login attempt

  Scenario: Existing user forgot password

  Scenario: Non-existing user forgot password

  Scenario: New user sign up

  Scenario: Administrator sign-in
