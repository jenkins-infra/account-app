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

  @wip @bug
  Scenario: Attempting a blank login

      Currently the app (stapler/java) does not check
      forms or require that the user enter any values for the
      username/password. Blindly clicking 'Login' ends up in a
      `javax.servlet.ServletException: javax.naming.InvalidNameException: [LDAP: error code 34 - invalid DN]`

    Given that I am unauthenticated
    When I attempt to login
    Then I should be given an error

  @wip
  Scenario: Existing user forgot password

      This scenario needs to be reworked at some point to not reset the same
      user that we need for other authentication scenarios, as it modifies the
      state in LDAP

    Given that I am unauthenticated
    And that I am an existing user
    When I reset a password
    Then I should be told to check my email

  @wip
  Scenario: Non-existing user forgot password
    Given that I am unauthenticated
    And I do not have an existing user
    When I reset a password
    Then I should be told to check my email

  @wip @bug
  Scenario: Attempting to reset a password should not confirm an identity

      It's generally bad security practice to confirm/deny the presence of a
      specific user ID to an unauthenticated user.

    Given that I am unauthenticated
    And I do not have an existing user
    When I reset a password
    Then I should be given an error
    And the presence of an account should not be confirmed

  Scenario: New user sign up
    Given that I am unauthenticated
    And I do not have an existing user
    When I sign up
    Then I should be able to login

  @admin
  Scenario: Administrator sign-in
    Given that I am an administrator
    When I attempt to login
    Then I should be given administrative options
