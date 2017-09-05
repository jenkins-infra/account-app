
Given(/^that I am unauthenticated$/) do
  # Default state
end

Given(/^that I am an administrator$/) do
  set_current DEFAULT_ADMIN, DEFAULT_PASS
end

Given(/^I do not have an existing user$/) do
  set_current 'notreal', 'notreal'
end

Given(/^that I am an existing user$/) do
  set_current DEFAULT_USER, DEFAULT_PASS
end



When(/^I navigate to the home page$/) do
  visit '/'
end

When(/^I attempt to login$/) do
  visit '/login'
  authenticate!
end

When(/^I reset a password$/) do
  reset_password!
end

When(/^I sign up$/) do
  pending # Write code here that turns the phrase above into concrete actions
end



Then(/^I should see a login screen$/) do
  login_screen?
end

Then(/^I should be able to login successfully$/) do
  login_screen?
  authenticate!
  logged_in?
end

Then(/^I should be given an error$/) do
  expect(page).to have_content 'Oops!'
end

Then(/^the presence of an account should not be confirmed$/) do
  expect(page).not_to have_content(@user)
end

Then(/^I should be told to check my email$/) do
  expect(page).to have_content 'Check your email for a password reset token'
end

Then(/^I should be given administrative options$/) do
  expect(page).to have_content 'Administration'
end

Then(/^I should be able to login$/) do
  pending # Write code here that turns the phrase above into concrete actions
end
