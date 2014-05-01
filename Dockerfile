FROM ubuntu:trusty

RUN apt-get install -y openjdk-6-jre unzip

RUN useradd --create-home accountapp
ADD bin /home/accountapp/bin
ADD target/accountapp.war /home/accountapp/bin/accountapp.war

EXPOSE 8080
USER accountapp

ENTRYPOINT /home/accountapp/bin/run.sh
