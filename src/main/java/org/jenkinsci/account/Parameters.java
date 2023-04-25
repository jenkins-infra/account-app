package org.jenkinsci.account;

import org.jenkinsci.account.config.LdapConfig;
import org.jenkinsci.account.config.MailConfig;

/**
 * Configuration of the application that needs to be set outside the application.
 *
 * @author Kohsuke Kawaguchi
 */
public class Parameters {

    private final String url;
    private final LdapConfig ldapConfig;
    private final MailConfig mailConfig;
    private final String circuitBreakerFile;

    public Parameters(String url, LdapConfig ldapConfig, MailConfig mailConfig, String circuitBreakerFile) {
        this.url = url;
        this.ldapConfig = ldapConfig;
        this.mailConfig = mailConfig;
        this.circuitBreakerFile = circuitBreakerFile;
    }

    /**
     * string like "ou=people,dc=acme,dc=com" that decides where new users are created.
     */
    public String newUserBaseDN() {
        return ldapConfig.getNewUserBaseDn();
    }

    /**
     * Coordinates to access LDAP.
     */
    public String managerDN() {
        return ldapConfig.getManagerDn();
    }
    public String managerPassword() {
        return ldapConfig.getManagerPassword();
    }
    public String server() {
        return ldapConfig.getServer();
    }

    /**
     * smtpServer: The SMTP server to connect to.
     * smtpSender: The sender email address used to send emails
     * smtpUser: Default user name for SMTP.
     * smtpAuth: If true, attempt to authenticate the user using the AUTH command.
     * smtpPassword: SMTP password for SMTP server.
     */
    public boolean smtpAuth() {
        return mailConfig.isSmtpAuth();
    }
    public String smtpServer() {
        return mailConfig.getSmtpServer();
    }
    public String smtpSender() {
        return mailConfig.getSmtpSender();
    }
    public String smtpUser() {
        return mailConfig.getSmtpUser();
    }
    public String smtpPassword() {
        return mailConfig.getSmtpPassword();
    }

    public int smtpPort() {
        return mailConfig.getSmtpPort();
    }

    /**
     * HTTP URL that this application is running. Something like '<a href="https://accounts.jenkins.io/">https://accounts.jenkins.io/</a>'. Must end with '/'.
     */
    public String url() {
        return url;
    }

    /**
     * File that activates a circuit breaker, a temporary shutdown of a sign-up service.
     */

    public String circuitBreakerFile() {
        return circuitBreakerFile;
    }
}
