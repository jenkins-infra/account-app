FROM jetty:jre8-alpine

LABEL \
  Description="Deploy Jenkins infra account app" \
  Project="https://github.com/jenkins-infra/account-app" \
  Maintainer="infra@lists.jenkins-ci.org"

ENV ELECTION_LOGDIR=/var/log/accountapp/elections
ENV CIRCUIT_BREAKER_FILE=/etc/accountapp/circuitBreaker.txt
ENV SMTP_SERVER=localhost
ENV JIRA_URL=https://issues.jenkins-ci.org
ENV APP_URL=https://accounts.jenkins.io/

EXPOSE 8080


# /home/jetty/.app is apparently needed by Stapler for some weird reason. O_O
RUN mkdir -p /home/jetty/.app &&\
    mkdir -p /etc/accountapp &&\
    mkdir -p $ELECTION_LOGDIR

COPY config.properties.example /etc/accountapp/config.properties.example
COPY circuitBreaker.txt /etc/accountapp/circuitBreaker.txt
COPY entrypoint.sh /entrypoint.sh

COPY build/libs/accountapp*.war /var/lib/jetty/webapps/ROOT.war

RUN chmod 0755 /entrypoint.sh &&\
    chown -R jetty:root /etc/accountapp &&\
    chown -R jetty:root /var/lib/jetty &&\
    chown -R jetty:root $ELECTION_LOGDIR

USER jetty

ENTRYPOINT /entrypoint.sh
