package org.jenkinsci.account;

import java.util.Date;

/**
 * Configuration of the application that needs to be set outside the application.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Parameters {
    /**
     * string like "ou=people,dc=acme,dc=com" that decides where new users are created.
     */
    String newUserBaseDN();

    /**
     * Coordinates to access LDAP.
     */
    String managerDN();
    String managerPassword();
    String server();

    /**
     * smtpServer: The SMTP server to connect to.
     * smtpUser: Default user name for SMTP.
     * smtpAuth: If true, attempt to authenticate the user using the AUTH command.
     * smtpPassword: SMTP password for SMTP server.
     */
    String smtpServer();
    String smtpUser();
    String smtpAuth();
    String smtpPassword();

    String recaptchaPublicKey();
    String recaptchaPrivateKey();

    /**
     * HTTP URL that this application is running. Something like 'http://jenkins-ci.org/account/'. Must end with '/'.
     */
    String url();

    /**
     * File that activates a circuit breaker, a temporary shutdown of a sign-up service.
     */
    String circuitBreakerFile();

    String electionCandidates();

    String electionLogDir();

    String electionOpen();

    String electionClose();
}
