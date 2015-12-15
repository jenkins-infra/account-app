package org.jenkinsci.account;

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

    String smtpServer();

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
}
