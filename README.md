# Jenkins Account Management

## Testing locally

Run `docker compose up -d ldap`

```shell
./gradlew appRun
```

This will get you a development server running at `http://localhost:8080`

## Testing locally against production

If you want to use real data you will need to set a few environment variables:

```shell
JIRA_USERNAME=<insert your jira username>
JIRA_PASSWORD=<insert your jira password>
JIRA_URL=https://issues.jenkins.io
LDAP_URL=ldap://localhost:1389/ # needs to be port forwarded see below
LDAP_PASSWORD=<insert the ldap admin password>
LDAP_MANAGER_DN=cn=admin,dc=jenkins-ci,dc=org
SMTP_SERVER=smtp.sendgrid.net
SMTP_AUTH=true
SMTP_USER=apikey
SMTP_PASSWORD=<insert sendgrid api key>
```

and port forward to the production ldap instance:

```shell
kubectl port-forward ldap-0 1389:389 1636:636 -n ldap
```

then run:

```shell
./gradlew appRun
```

## Packaging

For deploying to production, this app is packaged as a container.

To run the container locally:

```shell
export JIRA_USERNAME=<your-jira-username>
export JIRA_PASSWORD=<your-jira-password>
docker compose up --build app
```

## Makefile

`make run`: Runs the docker container

## SMTP

The account app support different types of SMTP configuration to send emails:

* Nothing is configured, the application try to connect on `localhost:25`
* `SMTP_AUTH` is set to false, the accountapp will connect on `$SMTP_SERVER:25`
* `SMTP_AUTH` is set to true, the accountapp will connect on `$SMTP_SERVER:587` with tls authentication
  and will use: `$SMTP_USER` with `$SMTP_PASSWORD`.
