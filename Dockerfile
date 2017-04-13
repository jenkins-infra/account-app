FROM jetty:jre8-alpine

# This is apparently needed by Stapler for some weird reason. O_O
RUN mkdir -p /home/jetty/.app

COPY build/libs/accountapp*.war /var/lib/jetty/webapps/ROOT.war

RUN mkdir -p /etc/accountapp
COPY etc/config.properties /etc/accountapp/config.properties.example

COPY entrypoint.sh /entrypoint.sh

RUN \
  chmod 0755 /entrypoint.sh &&\
  chown -R jetty: /etc/accountapp

EXPOSE 8080

USER jetty

# Overriding the ENTRYPOINT from our parent to make it easier to tell it about
# our config.properties which the app needs
ENTRYPOINT /entrypoint.sh
