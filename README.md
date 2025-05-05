# Jenkins Account Management

## Testing locally

With a Docker Engine in "Linux Container" mode and the `compose` plugin installed,
run the command `docker compose up --build -d` which will:

- Build the application (in a container)
- Start a LDAP server with fixtures

You can also run only the LDAP stack with `docker compose up -d ldap-data` and then `./gradlew appRun` for local development.

Both cases will get you a development server running at <http://localhost:8080>.

The default admin username is `kohsuke` and its password is `password` (see the mock-ldap/ directory).

## Packaging

For deploying to production, this app is packaged as a container.

To run the container locally:

```shell
docker compose up --build app
```

## SMTP

The account app support different types of SMTP configuration to send emails:

- Nothing is configured, the application try to connect on `localhost:25`
- `SMTP_AUTH` is set to false, the accountapp will connect on `$SMTP_SERVER:25`
- `SMTP_AUTH` is set to true, the accountapp will connect on `$SMTP_SERVER:587` with tls authentication
  and will use: `$SMTP_USER` with `$SMTP_PASSWORD`.
