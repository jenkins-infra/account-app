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

  @wip @bug
  Scenario: Add SSH public keys

      This was broken as of cf20102bef6afaf92c39c885f799830fd71cd050 and needs
      to be fixed

    Given I have added an SSH public key
    When I save my profile
    Then my profile should have the SSH public key

  @wip
  Scenario: Link a GitHub.com Identity
    Given I have a GitHub.com account
    When I link my profile
    Then I should be asked to authorize the application

  Scenario: Change password
    Given I have a entered a new password
    When I save my profile
    And I logout
    Then I should be able to login successfully
    And my password should have been changed
