

Given(/^that I am unauthenticated$/) do
  # Default state
end

When(/^I navigate to the home page$/) do
  visit '/'
end

Then(/^I should see a login screen$/) do
  expect(page).to have_css('#userid')
  expect(page).to have_css('#login_password')
end
