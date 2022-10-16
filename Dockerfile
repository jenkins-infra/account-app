FROM eclipse-temurin:8 AS build

WORKDIR /app
COPY . .
RUN ./gradlew --no-daemon --info war

FROM jetty:jre8 AS production

LABEL \
  description="Deploy Jenkins infra account app" \
  project="https://github.com/jenkins-infra/account-app" \
  maintainer="infra@lists.jenkins-ci.org"

ENV DD_AGENT_SERVICE_PORT="8126"
ENV CIRCUIT_BREAKER_FILE=/etc/accountapp/circuitBreaker.txt
ENV SMTP_SERVER=localhost
ENV JIRA_URL=https://issues.jenkins.io
ENV APP_URL=http://accounts.jenkins.io/

EXPOSE 8080

USER root

# /home/jetty/.app is apparently needed by Stapler for some weird reason. O_O
RUN \
  mkdir -p /home/jetty/.app &&\
  mkdir -p /etc/accountapp

COPY config.properties.example /etc/accountapp/config.properties.example
COPY circuitBreaker.txt /etc/accountapp/circuitBreaker.txt
COPY entrypoint.sh /entrypoint.sh

ADD https://search.maven.org/remote_content?g=com.datadoghq&a=dd-java-agent&v=0.9.0 /home/jetty/dd-java-agent.jar

COPY --chown=jetty:root --from=build /app/build/libs/accountapp*.war /var/lib/jetty/webapps/ROOT.war

RUN chmod 0755 /entrypoint.sh &&\
    chown -R jetty:root /etc/accountapp &&\
    chown -R jetty:root /var/lib/jetty &&\
    chown -R jetty:root /home/jetty/dd-java-agent.jar

USER jetty

ENTRYPOINT ["bash","/entrypoint.sh"]
