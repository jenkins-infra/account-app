#!/bin/sh

set -e

exec java \
    -Dcom.sun.jndi.ldap.connect.pool=true \
    -Dcom.sun.jndi.ldap.connect.pool.protocol="plain ssl" \
    -Dcom.sun.jndi.ldap.connect.pool.maxsize=0 \
    -Dcom.sun.jndi.ldap.connect.pool.prefsize=10 \
    -Dcom.sun.jndi.ldap.connect.pool.timeout=180000 \
    -Dcom.sun.jndi.ldap.connect.timeout=1000 \
    -Ddd.agent.host="$DD_AGENT_SERVICE_HOST" \
    -Ddd.agent.port="$DD_AGENT_SERVICE_PORT" \
    -javaagent:/home/jetty/dd-java-agent.jar \
    -jar "$JETTY_HOME/start.jar"
