require 'capybara/cucumber'
require 'capybara/poltergeist'

require_relative 'helpers'

Capybara.app_host = 'http://run:8080/'
Capybara.javascript_driver = :poltergeist
Capybara.default_driver = :poltergeist

World(AccountHelpers)
World(ProfileHelpers)

DEFAULT_USER = ENV['DEFAULT_USER'] || 'alice'
DEFAULT_ADMIN = ENV['DEFAULT_ADMIN'] || 'kohsuke'
DEFAULT_PASS = ENV['DEFAULT_PASS'] || 'password'
DEFAULT_ADMIN_PASS = ENV['DEFAULT_ADMIN_PASS'] || DEFAULT_PASS
