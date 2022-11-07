# Jenkins Account Management

## Testing locally

Run `docker compose up -d ldap`

```shell
./gradlew appRun
```

This will get you a development server running at http://localhost:8080

### Emails

Emails are send to a local mail server and not forwarded on, you can see them at http://localhost:3000.

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
