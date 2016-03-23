FROM jetty:jre8

ADD build/libs/accountapp*.war /var/lib/jetty/webapps/account.war

# This is apparently needed by Stapler for some weird reason. O_O
RUN mkdir -p /home/jetty/.app

RUN mkdir -p /etc/accountapp

EXPOSE 8080

# Overriding the CMD from our parent to make it easier to tell it about
# our config.properties which the app needs
CMD java -DCONFIG=/etc/accountapp/config.properties -jar "$JETTY_HOME/start.jar"
