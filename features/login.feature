Feature: Log into the account app


  Scenario: Navigating to the app
    Given that I am unauthenticated
    When I navigate to the home page
    Then I should see a login screen

  Scenario: Existing user login
    Given that I am an existing user
    When I navigate to the home page
    Then I should be able to login successfully

  Scenario: Non-existing user login attempt
    Given that I am unauthenticated
    And I do not have an existing user
    When I attempt to login
    Then I should be given an error

  @wip @regression
  Scenario: Attempting a blank login

      Currently the app (stapler/java) does not check
      forms or require that the user enter any values for the
      username/password. Blindly clicking 'Login' ends up in a
      `javax.servlet.ServletException: javax.naming.InvalidNameException: [LDAP: error code 34 - invalid DN]`

    Given that I am unauthenticated
    When I attempt to login
    Then I should be given an error


  Scenario: Existing user forgot password

  Scenario: Non-existing user forgot password

  Scenario: New user sign up

  Scenario: Administrator sign-in
