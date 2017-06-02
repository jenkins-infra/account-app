# Jenkins Account Management/Sign-up App

## Testing locally

First, set up a tunnel to Jenkins LDAP server. Run the following command and
keep the terminal open:

    ssh -4 -L 9389:localhost:389 ldap.jenkins.io

Create `config.properties` in the same directory as `pom.xml`. See the
`Parameters` class for the details, but it should look something like the
following:

    server=ldap://localhost:9389/
    managerDN=cn=admin,dc=jenkins-ci,dc=org
    newUserBaseDN=ou=people,dc=jenkins-ci,dc=org
    smtpServer=localhost
    managerPassword=*****
    circuitBreakerFile=./circuitBreaker.txt
    url=http://localhost:8080/account/

Finally, run the application with Jetty, then access `http://localhost:8080/`:

    ./gradlew -Djira.url=https://issues.jenkins-ci.org/ -Djira.username=kohsuke -Djira.password=... -Durl=ldap://localhost:9389 -Dpassword=... jettyRun

(As you can see above, this connects your test instance to the actual LDAP
server, so the data you'll be seeing is real.

The command line system properties are for JIRA LDAP sync tool. JIRA user account you are providing has to have the system admin access to JIRA.
TODO: feed this data from config.properties

### Docker Compose
A docker compose file can be used for testing purpose.

_Require ssh tunnel to an ldap server and an WAR archive_

* Create the file ```.env``` used by docker-compose to load configuration
.env example
```
    APP_URL=http://localhost:8080/
    ELECTION_CANDIDATES=alice,bob
    ELECTION_CLOSE=2038/01/19
    ELECTION_OPEN=1970/01/01
    JIRA_USERNAME=<insert your jira username>
    JIRA_PASSWORD=<insert your jira password>
    JIRA_URL=https://issues.jenkins-ci.org
    LDAP_URL=server=ldap://localhost:9389/
    LDAP_PASSWORD=<insert your ldap password>
    LDAP_MANAGER_DN=cn=admin,dc=jenkins-ci,dc=org
    LDAP_NEW_USER_BASE_DN=ou=people,dc=jenkins-ci,dc=org
    RECAPTCHA_PRIVATE_KEY=recaptcha_private_key
    RECAPTCHA_PUBLIC_KEY=recaptcha_public_key
    SMTP_SERVER=smtp.jenkins.io
    SMTP_USER=user@jenkins.io
    SMTP_AUTH=true
    SMTP_PASSWORD=password
```
* Run docker-compose 
```docker-compose up --build accountapp```

## Packaging

For deploying to production, this app gets containerized. The container expects
to see `/etc/accountapp` mounted from outside that contains the above mentioned
`config.properties`


To run the container locally, build it then:

    docker run -ti --net=host  -v `pwd`:/etc/accountapp jenkinsciinfra/account-app:latest

## Configuration
Instead of mounting the configuration file from an external volume,
we may want to use environment variable.

**Those two options are mutually exclusive.**

```
* APP_URL
* CIRCUIT_BREAKER_FILE
* ELECTION_CANDIDATES   coma separated list of candidates
* ELECTION_CLOSE   date election will close. yyyy/MM/dd
* ELECTION_OPEN    date election will open. yyyy/MM/dd
* ELECTION_LOGDIR
* JIRA_PASSWORD
* JIRA_URL
* JIRA_USERNAME
* LDAP_MANAGER_DN
* LDAP_NEW_USER_BASE_DN
* LDAP_PASSWORD
* LDAP_URL
* RECAPTCHA_PUBLIC_KEY
* RECAPTCHA_PRIVATE_KEY
* SMTP_SERVER
* SMTP_USER
* SMTP_PASSWORD
* SMTP_AUTH
```

## Makefile

``` make build```: Build build/libs/accountapp-2.5.war and docker image
``` make run ```: Run docker container
``` make clean ```: Clean build environment

## SMTP
The accountapp support different types of SMTP configuration to send emails.
* Nothing is configured, the application try to connect on localhost:25
* SMTP_AUTH is set to false, the accountapp will connect on  $SMTP_SERVER:25
* SMTP_AUTH is set to true, the accountapp will connect on $SMTP_SERVER:587 with tls authentication
  and will use username: $SMTP_USER with password $SMTP_PASSWORD.
