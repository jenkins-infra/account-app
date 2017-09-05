module AccountHelpers
  def login_screen?
    expect(page).to have_css('#userid')
    expect(page).to have_css('#login_password')
  end

  def set_current(username, password)
    @user = username
    @password = password
  end

  def authenticate!
    fill_in('userid', :with => @user)
    fill_in('password', :with => @password)
    click_button('Login')
  end

  def logged_in?
    expect(page).to have_content('Logout')
  end

  def reset_password!
    visit '/passwordReset'
    fill_in('id', :with => @user)
    click_button 'Reset password'
  end
end

module ProfileHelpers
  def visit_profile
    visit '/myself'
  end
end
