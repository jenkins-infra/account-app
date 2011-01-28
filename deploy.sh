#!/bin/bash -ex
# deploy to the production server
mvn package
scp target/accountapp.war accountapp@cucumber:~
