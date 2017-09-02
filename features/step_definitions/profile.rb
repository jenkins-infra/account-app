

Given(/^I have entered a new email$/) do
  fill_in('email', :with => 'alicia@example.com')
end

Given(/^I have entered a new first and last name$/) do
  fill_in('firstName', :with => 'Alicia')
  fill_in('lastName', :with => 'Benicia')
end

Given(/^I have a entered a new password$/) do
  password = 'Password!'
  fill_in('password', :with => @password)
  fill_in('newPassword1', :with => password)
  fill_in('newPassword2', :with => password)
  set_current @user, password
end


When(/^I view my profile$/) do
  visit_profile
end

When(/^I save my profile$/) do
  click_button 'Update'
end

When(/^I logout$/) do
  click_link 'Logout'
end



Then(/^my profile should have the new first and last name$/) do
  visit_profile
  expect(find_field('firstName').value).to eql('Alicia')
  expect(find_field('lastName').value).to eql('Benicia')
end

Then(/^my profile should have the new email$/) do
  visit_profile
  expect(find_field('email').value).to eql('alicia@example.com')
end


Then(/^my password should have been changed$/) do
  visit_profile
  password = 'password'
  fill_in('password', :with => @password)
  fill_in('newPassword1', :with => password)
  fill_in('newPassword2', :with => password)
  set_current @user, password
  click_button 'Update'
end

