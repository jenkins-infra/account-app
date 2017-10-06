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
    : "${RECAPTCHA_PUBLIC_KEY:? Recaptcha private key}"
    : "${RECAPTCHA_PRIVATE_KEY:? Recaptcha private key}"
    : "${APP_URL:? Application url required}"
    : "${LDAP_MANAGER_DN:? Require ldap manager_DN}"
    : "${LDAP_NEW_USER_BASE_DN:? Require ldap new user base DN}"
    : "${CIRCUIT_BREAKER_FILE:? Require circuitBreaker file}"

    # Elections configurations
    : "${ELECTION_CANDIDATES:? Required coma separated list of candidates}"
    : "${ELECTION_CLOSE:? Required date election will close. yyyy/MM/dd}"
    : "${ELECTION_OPEN:? date election will open. yyyy/MM/dd }"
    : "${ELECTION_LOGDIR:? Require election log directory }"
    : "${SENIORITY:? Require seniority criteria in month }"
    : "${SEATS:? Require number of seats for election }"

    #Directory to store collected votes. assume this path is well persisted/backup

    if [ ! -d "${ELECTION_LOGDIR}" ]; then
        mkdir -p "${ELECTION_LOGDIR}"
        chown jetty: "$ELECTION_LOGDIR"
    fi

    cp /etc/accountapp/config.properties.example /etc/accountapp/config.properties

    # Using # as variables may contain / 
    sed -i "s#SMTP_SERVER#$SMTP_SERVER#" /etc/accountapp/config.properties
    sed -i "s#SMTP_USER#$SMTP_USER#" /etc/accountapp/config.properties
    sed -i "s#SMTP_AUTH#$SMTP_AUTH#" /etc/accountapp/config.properties
    sed -i "s#SMTP_PASSWORD#$SMTP_PASSWORD#" /etc/accountapp/config.properties
    sed -i "s#LDAP_URL#$LDAP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_PASSWORD#$LDAP_PASSWORD#" /etc/accountapp/config.properties
    sed -i "s#RECAPTCHA_PUBLIC_KEY#$RECAPTCHA_PUBLIC_KEY#" /etc/accountapp/config.properties
    sed -i "s#RECAPTCHA_PRIVATE_KEY#$RECAPTCHA_PRIVATE_KEY#" /etc/accountapp/config.properties
    sed -i "s#APP_URL#$APP_URL#" /etc/accountapp/config.properties
    sed -i "s#LDAP_MANAGER_DN#$LDAP_MANAGER_DN#" /etc/accountapp/config.properties
    sed -i "s#LDAP_NEW_USER_BASE_DN#$LDAP_NEW_USER_BASE_DN#" /etc/accountapp/config.properties
    sed -i "s#CIRCUIT_BREAKER_FILE#$CIRCUIT_BREAKER_FILE#" /etc/accountapp/config.properties
    sed -i "s#ELECTION_CANDIDATES#$ELECTION_CANDIDATES#" /etc/accountapp/config.properties
    sed -i "s#ELECTION_OPEN#$ELECTION_OPEN#" /etc/accountapp/config.properties
    sed -i "s#ELECTION_CLOSE#$ELECTION_CLOSE#" /etc/accountapp/config.properties
    sed -i "s#ELECTION_LOGDIR#$ELECTION_LOGDIR#" /etc/accountapp/config.properties
    sed -i "s#SENIORITY#$SENIORITY#" /etc/accountapp/config.properties
    sed -i "s#SEATS#$SEATS#" /etc/accountapp/config.properties
}

if [ ! -f /etc/accountapp/config.properties ]; then
    init_config_properties
fi

exec java -DCONFIG=/etc/accountapp/config.properties -Durl="$LDAP_URL" -Dpassword="$LDAP_PASSWORD" -Djira.username="$JIRA_USERNAME" -Djira.password="$JIRA_PASSWORD" -Djira.url="$JIRA_URL" -jar "$JETTY_HOME/start.jar"
