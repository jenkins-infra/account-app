Feature: Users should be able to edit their own settings

  Background: An existing user
    Given that I am an existing user
    When I attempt to login
    And I view my profile

  Scenario: Update First/Last Name
    Given I have entered a new first and last name
    When I save my profile
    Then my profile should have the new first and last name

  Scenario: Change email address
    Given I have entered a new email
    When I save my profile
    Then my profile should have the new email

  Scenario: Add SSH public keys

  Scenario: Link a GitHub.com Identity

  Scenario: Change password
    Given I have a entered a new password
    When I save my profile
    And I logout
    Then I should be able to login successfully
    And my password should have been changed
