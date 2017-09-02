

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
    fill_in('Userid', :with => @user)
    fill_in('Password', :with => @password)
    click_button('Login')
  end

  def logged_in?
    expect(page).to have_content('Logout')
  end
end
