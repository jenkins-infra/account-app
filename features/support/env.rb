require 'capybara/cucumber'
require 'capybara/poltergeist'

require_relative 'helpers'

Capybara.app_host = 'http://run:8080/'
Capybara.javascript_driver = :poltergeist
Capybara.default_driver = :poltergeist

World(AccountHelpers)
World(ProfileHelpers)

DEFAULT_USER = ENV['DEFAULT_USER'] or 'alice'
DEFAULT_ADMIN = ENV['DEFAULT_ADMIN'] or 'kohsuke'
DEFAULT_PASS = ENV['DEFAULT_PASS'] or 'password'
