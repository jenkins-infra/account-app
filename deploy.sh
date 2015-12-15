#!/bin/bash -ex
# deploy to the production server
mvn clean package
scp target/accountapp.war accountapp@cucumber:~
ssh cucumber sudo /etc/init.d/accountapp stop
sleep 3
ssh cucumber sudo /etc/init.d/accountapp start
