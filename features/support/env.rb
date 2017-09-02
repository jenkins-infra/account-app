require 'capybara/cucumber'
require 'capybara/poltergeist'

require_relative 'helpers'

Capybara.app_host = 'http://run:8080/'
Capybara.javascript_driver = :poltergeist
Capybara.default_driver = :poltergeist

World(AccountHelpers)
