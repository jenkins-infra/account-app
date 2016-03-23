# Jenkins Account Management/Sign-up App

[![Build Status](http://ci.jenkins-ci.org/view/Infrastructure/job/infra_accountapp/badge/icon)](http://ci.jenkins-ci.org/view/Infrastructure/job/infra_accountapp/)

## Testing locally

First, set up a tunnel to Jenkins LDAP server. Run the following command and
keep the terminal open:

    ssh -L 9389:localhost:389 cucumber.jenkins-ci.org

Create `config.properties` in the same directory as `pom.xml`. See the
`Parameters` class for the details, but it should look something like the
following:

    server=ldap://localhost:9389/
    managerDN=cn=admin,dc=jenkins-ci,dc=org
    newUserBaseDN=ou=people,dc=jenkins-ci,dc=org
    smtpServer=localhost
    recaptchaPublicKey=6Ld--8ASAAAAANHmHaM1sdSYshtmXTin1BNtaw86
    recaptchaPrivateKey=*****
    managerPassword=*****
    circuitBreakerFile=./circuitBreaker.txt
    url=http://localhost:8080/account/

Finally, run the application with Jetty, then access `http://localhost:8080/`:

    ./gradlew jettyRun

(As you can see above, this connects your test instance to the actual LDAP
server, so the data you'll be seeing is real.


## Packaging

For deploying to production, this app gets containerized. The container expects
to see `/etc/accountapp` mounted from outside that contains the abovementioned
`config.properties`


To run the container locally, build it then:

    docker run -ti --net=host  -v `pwd`:/etc/accountapp jenkinsciinfra/account-app:latest
