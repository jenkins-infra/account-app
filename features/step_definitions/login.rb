
Given(/^that I am unauthenticated$/) do
  # Default state
end

Given(/^I do not have an existing user$/) do
  set_current 'notreal', 'notreal'
end

Given(/^that I am an existing user$/) do
  set_current 'kohsuke', 'password'
end


When(/^I navigate to the home page$/) do
  visit '/'
end

When(/^I attempt to login$/) do
  visit '/login'
  authenticate!
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
