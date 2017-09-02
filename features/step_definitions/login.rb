
Given(/^that I am unauthenticated$/) do
  # Default state
end

Given(/^that I am an existing user$/) do
  set_current 'kohsuke', 'password'
end

When(/^I navigate to the home page$/) do
  visit '/'
end

Then(/^I should see a login screen$/) do
  login_screen?
end

Then(/^I should be able to login successfully$/) do
  login_screen?
  fill_in('Userid', :with => @user)
  fill_in('Password', :with => @password)
  click_button('Login')
  expect(page).to have_content('Logout')
end
