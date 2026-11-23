#!/bin/bash

set -ex

CUSTOM_CACERT_FILE=/var/lib/jetty/resources/localcacerts
CUSTOM_CACERT_PASS=changeit

declare -a main_command

main_command=("java"
    "-Dcom.sun.jndi.ldap.connect.pool=true"
    "-Dcom.sun.jndi.ldap.connect.pool.protocol='plain ssl'"
    "-Dcom.sun.jndi.ldap.connect.pool.maxsize=0"
    "-Dcom.sun.jndi.ldap.connect.pool.prefsize=10"
    "-Dcom.sun.jndi.ldap.connect.pool.timeout=180000"
    "-Dcom.sun.jndi.ldap.connect.timeout=1000"
)

if [ -n "${DD_AGENT_SERVICE_HOST}" ] && [ -n "${DD_AGENT_SERVICE_PORT}" ]
then
    main_command+=("-Ddd.agent.host=${DD_AGENT_SERVICE_HOST}" "-Ddd.agent.port=${DD_AGENT_SERVICE_PORT}" "-javaagent:/home/jetty/dd-java-agent.jar")
fi

if [ -f "${CUSTOM_CERT_FILE}" ]
then
    if [ ! -f ${CUSTOM_CACERT_FILE} ]
    then
        keytool -import -trustcacerts -alias localcacerts -noprompt -storepass "${CUSTOM_CACERT_PASS}" -file "${CUSTOM_CERT_FILE}" -keystore "${CUSTOM_CACERT_FILE}"
    fi
    main_command+=("-Djavax.net.ssl.trustStore=${CUSTOM_CACERT_FILE}" "-Djavax.net.ssl.trustStorePassword=${CUSTOM_CACERT_PASS}")
fi

main_command+=("-jar" "${JETTY_HOME}/start.jar")
exec "${main_command[@]}"
