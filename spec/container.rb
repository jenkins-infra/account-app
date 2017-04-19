require "serverspec"
require "docker"

files=[
  '/entrypoint.sh',
  '/etc/accountapp/config.properties.example',
  '/var/lib/jetty/webapps/ROOT.war'
]

directories=[
    '/home/jetty/.app',
    '/etc/accountapp',
    '/var/lib/jetty/webapps/'
]

image="spec/accountapp_spec:latest"

apks =[
  'musl',
  'busybox',
  'zlib',
  'apk-tools',
  'java-cacerts',
  'java-common',
  'openjdk8-jre-lib',
  'openjdk8-jre-base',
  'openjdk8-jre',
]

forbidden_apks =[
  'openjdk7-jre-base',
  'openjdk7-jre-lib',
  'openjdk7-jre',
]

describe "Container" do
    before(:all) do
       Docker.options[:read_timeout] = 100000
       Docker.options[:write_timeout] = 100000
       @image=Docker::Image.build_from_dir('.',{ 't' => image })
       @container = Docker::Container.create(
         'Image'      => image,
         'Entrypoint' => ["sh", "-c", "tail -f /dev/null"],
         'Env'        => [
             "TERM=xterm"
          ])
       @container.start

       set :os, family: :alpine
       set :backend, :docker
       set :docker_container, @container.id
    end

    after(:all) do
        @container.kill
        @container.delete(:force => true)
        @image.remove(:force => true)
    end
    
    directories.each do |name|
      it "should have directory: #{name}" do
        expect(file(name)).to be_a_directory
      end
    end

    files.each do |name|
      it "should have file: #{name}" do
        expect(file(name)).to be_a_file
      end
    end

    apks.each do |apk| 
      it "should have #{apk} installed" do
        expect(package(apk)).to be_installed.by('apk')
      end
    end

    forbidden_apks.each do |apk| 
      it "shouldn't have #{apk} installed" do
        expect(package(apk)).not_to be_installed
      end
    end
end
