# Jenkins Account Management/Sign-up App

## Testing locally

First, set up a tunnel to Jenkins LDAP server. Run the following command and
keep the terminal open:

    ssh -L 9389:localhost:389 ldap.jenkins.io

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


## Packaging

For deploying to production, this app gets containerized. The container expects
to see `/etc/accountapp` mounted from outside that contains the abovementioned
`config.properties`


To run the container locally, build it then:

    docker run -ti --net=host  -v `pwd`:/etc/accountapp jenkinsciinfra/account-app:latest

## Configuration
Instead of mounting the configuration file from an external volume, 
we may want to use environment variable.
Those two options are mutually exclusive.

* APP_URL 
* CIRCUIT_BREAKER_FILE ('/etc/accountapp/circuitBreaker')
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

## Tests
We can run basic tests

Required:
  ruby-2.2

Run:
```
  bundle install
  rake test
```
