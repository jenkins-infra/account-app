FROM jetty:jre8

ADD build/libs/accountapp*.war /var/lib/jetty/webapps/ROOT.war

# This is apparently needed by Stapler for some weird reason. O_O
RUN mkdir -p /home/jetty/.app

RUN mkdir -p /etc/accountapp

EXPOSE 8080

# Overriding the ENTRYPOINT from our parent to make it easier to tell it about
# our config.properties which the app needs
ENTRYPOINT java -DCONFIG=/etc/accountapp/config.properties -Durl="$LDAP_URL" -Dpassword="$LDAP_PASSWORD" -Djira.username="$JIRA_USERNAME" -Djira.password="$JIRA_PASSWORD" -Djira.url="$JIRA_URL" -jar "$JETTY_HOME/start.jar"
