FROM jetty:jre8-alpine

LABEL \
  Description="Deploy Jenkins infra account app" \
  Project="https://github.com/jenkins-infra/account-app" \
  Maintainer="infra@lists.jenkins-ci.org"

EXPOSE 8080

ENV CIRCUIT_BREAKER_FILE /etc/accountapp/circuitBreaker.txt

# /home/jetty/.app is apparently needed by Stapler for some weird reason. O_O
RUN \
  mkdir -p /home/jetty/.app &&\ 
  mkdir -p /etc/accountapp

COPY config.properties.example /etc/accountapp/config.properties.example
COPY circuitBreaker.txt /etc/accountapp/circuitBreaker.txt
COPY entrypoint.sh /entrypoint.sh

RUN \
  chmod 0755 /entrypoint.sh &&\
  chown -R jetty:root /etc/accountapp

COPY build/libs/accountapp*.war /var/lib/jetty/webapps/ROOT.war

USER jetty

ENTRYPOINT /entrypoint.sh
