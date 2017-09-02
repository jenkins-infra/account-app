require 'capybara/cucumber'
require 'capybara/poltergeist'


Capybara.app_host = 'https://accounts.jenkins.io/'
Capybara.javascript_driver = :poltergeist
Capybara.default_driver = :poltergeist
