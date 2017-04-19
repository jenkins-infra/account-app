require "serverspec"
require "docker"

name='spec/accountapp:latest'

describe "Dockerfile" do 
  before (:all) do
    Docker.options[:read_timeout] = 100000
    Docker.options[:write_timeout] = 100000

    @image=Docker::Image.build_from_dir('.',{ 't' => name })
    set :os, family: :alpine
    set :backend, :dockerfile
    set :docker_image, @image.id
  end

  after (:all) do
    @image.remove(:force => true)
  end

  it "should be define" do
    expect(@image).not_to be_nil
  end

  it "should have /entrypoint.sh" do
    expect(@image.json["Config"]["Entrypoint"]).to include("/entrypoint.sh")
  end

  it "should have a label Description defined" do
    expect(@image.json["ContainerConfig"]["Labels"]["Description"]).not_to be_nil
  end

  it "should have a label Project defined" do
    expect(@image.json["ContainerConfig"]["Labels"]["Project"]).to eq("https://github.com/jenkins-infra/account-app")
  end

  it "should have a label Maintainer defined" do
    expect(@image.json["ContainerConfig"]["Labels"]["Maintainer"]).to eq("infra@lists.jenkins-ci.org")
  end

  it "should have env JETTY_HOME defined" do
      expect(@image.json["ContainerConfig"]["Env"]).to include("JETTY_HOME=/usr/local/jetty")
  end
  it "should have env JAVA_HOME defined" do
      expect(@image.json["ContainerConfig"]["Env"]).to include("JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk/jre")
  end
  it "should have port 8080 open" do
    expect(@image.json["ContainerConfig"]["ExposedPorts"]["8080/tcp"]).not_to be_nil
  end
  it "should run as jetty user" do
    expect(@image.json["ContainerConfig"]["User"]).to eq("jetty")
  end
end
