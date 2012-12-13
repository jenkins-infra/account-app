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

    String managerDN();
    String managerPassword();
    String server();

    String smtpServer();

    String recaptchaPublicKey();
    String recaptchaPrivateKey();

    String rootDir();
}
