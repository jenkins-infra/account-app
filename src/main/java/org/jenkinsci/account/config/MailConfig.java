package org.jenkinsci.account.config;

public class MailConfig {

    private final String smtpServer;
    private final String smtpUser;
    private final String smtpPassword;
    private final boolean smtpAuth;

    public MailConfig(String smtpServer, String smtpUser, String smtpPassword, boolean smtpAuth) {
        this.smtpServer = smtpServer;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }
}
