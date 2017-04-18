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
    : "${RECAPTCHA_PUBLIC_KEY:? Recaptcha private key}"
    : "${RECAPTCHA_PRIVATE_KEY:? Recaptcha private key}"
    : "${APP_URL:? Application url required}"
    : "${LDAP_MANAGER_DN:? Require ldap manager_DN}"
    : "${LDAP_NEW_USER_BASE_DN:? Require ldap new user base DN}"
    : "${CIRCUIT_BREAKER_FILE:=/etc/accountapp/circuitBreaker}"


    cp /etc/accountapp/config.properties.example /etc/accountapp/config.properties

    # Using # as variable may / 
    sed -i "s#SMTP_SERVER#$SMTP_SERVER#" /etc/accountapp/config.properties
    sed -i "s#LDAP_URL#$LDAP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_PASSWORD#$LDAP_PASSWORD#" /etc/accountapp/config.properties
    sed -i "s#RECAPTCHA_PUBLIC_KEY#$RECAPTCHA_PUBLIC_KEY#" /etc/accountapp/config.properties
    sed -i "s#RECAPTCHA_PRIVATE_KEY#$RECAPTCHA_PRIVATE_KEY#" /etc/accountapp/config.properties
    sed -i "s#APP_URL#$APP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_MANAGER_DN#$LDAP_MANAGER_DN#" /etc/accountapp/config.properties
    sed -i "s#LDAP_NEW_USER_BASE_DN#$LDAP_NEW_USER_BASE_DN#" /etc/accountapp/config.properties
    sed -i "s#CIRCUIT_BREAKER_FILE#$CIRCUIT_BREAKER_FILE#" /etc/accountapp/config.properties
}

if [ ! -f /etc/accountapp/config.properties ]; then
    init_config_properties
fi

exec java -DCONFIG=/etc/accountapp/config.properties -Durl="$LDAP_URL" -Dpassword="$LDAP_PASSWORD" -Djira.username="$JIRA_USERNAME" -Djira.password="$JIRA_PASSWORD" -Djira.url="$JIRA_URL" -jar "$JETTY_HOME/start.jar"
