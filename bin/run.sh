#!/bin/bash
cd "$(dirname "$0")"
exec java -DCONFIG=/etc/accountapp/config.properties -jar jetty-runner.jar --port 8080 --path /account accountapp.war