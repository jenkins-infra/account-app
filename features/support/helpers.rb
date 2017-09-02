

module AccountHelpers
  def login_screen?
    expect(page).to have_css('#userid')
    expect(page).to have_css('#login_password')
  end

  def set_current(username, password)
    @user = username
    @password = password
  end
end
