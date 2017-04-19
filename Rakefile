require 'docker'

@image = "account-app:latest"

# Define tasks
#
desc "Build Docker Image #{@image}"
task :build do
  Docker::Image.build_from_dir('.',{ 't' => @image}) do |v|
    if (log = JSON.parse(v)) && log.has_key?("stream")
      $stdout.puts log["stream"]
    end
  end
end

namespace :test do
  desc "Run Dockerfile tests for #{@image}"
  task :dockerfile do
    sh "rspec spec/dockerfile.rb -f d -cb"
  end

  desc "Run Container tests for #{@image}"
  task :container do
    sh "rspec spec/container.rb -f d -cb"
  end

  task :all => ['dockerfile','containers']
end

desc "Install gem dependencies for tests"
task :init do
    sh "bundle install"
end

desc "Run all spec files"
task :test => ["test:dockerfile","test:container"]
