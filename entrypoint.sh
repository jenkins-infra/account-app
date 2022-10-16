#!/bin/sh

set -e 

init_config_properties() {
    : "${LDAP_URL:?Ldap url required}"
    : "${LDAP_PASSWORD:?Ldap password required}"
    : "${JIRA_USERNAME:?Jira user required}"
    : "${JIRA_PASSWORD:?Jira password required}"
    : "${JIRA_URL:? Jira url required}"

    # /etc/accountapp/config.properties
    : "${SMTP_SERVER:? SMTP Server required}"
    : "${SMTP_USER:? SMTP User required}"
    : "${SMTP_AUTH:? SMTP Auth required}"
    : "${SMTP_PASSWORD:? SMTP Password required}"
    : "${APP_URL:? Application url required}"
    : "${LDAP_MANAGER_DN:? Require ldap manager_DN}"
    : "${LDAP_NEW_USER_BASE_DN:? Require ldap new user base DN}"
    : "${CIRCUIT_BREAKER_FILE:? Require circuitBreaker file}"

    #Directory to store collected votes. assume this path is well persisted/backup

    cp /etc/accountapp/config.properties.example /etc/accountapp/config.properties

    # Using # as variables may contain / 
    sed -i "s#SMTP_SERVER#$SMTP_SERVER#" /etc/accountapp/config.properties
    sed -i "s#SMTP_USER#$SMTP_USER#" /etc/accountapp/config.properties
    sed -i "s#SMTP_AUTH#$SMTP_AUTH#" /etc/accountapp/config.properties
    sed -i "s#SMTP_PASSWORD#$SMTP_PASSWORD#" /etc/accountapp/config.properties
    sed -i "s#LDAP_URL#$LDAP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_PASSWORD#$LDAP_PASSWORD#" /etc/accountapp/config.properties
    sed -i "s#APP_URL#$APP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_MANAGER_DN#$LDAP_MANAGER_DN#" /etc/accountapp/config.properties
    sed -i "s#LDAP_NEW_USER_BASE_DN#$LDAP_NEW_USER_BASE_DN#" /etc/accountapp/config.properties
    sed -i "s#CIRCUIT_BREAKER_FILE#$CIRCUIT_BREAKER_FILE#" /etc/accountapp/config.properties
}

if [ ! -f /etc/accountapp/config.properties ]; then
    init_config_properties
fi

exec java -DCONFIG=/etc/accountapp/config.properties \
    -Durl="$LDAP_URL" \
    -Dusername="$LDAP_MANAGER_DN" \
    -DbaseDN="$LDAP_NEW_USER_BASE_DN" \
    -Dpassword="$LDAP_PASSWORD" \
    -Djira.username="$JIRA_USERNAME" \
    -Djira.password="$JIRA_PASSWORD" \
    -Djira.url="$JIRA_URL" \
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
