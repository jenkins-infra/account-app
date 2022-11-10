package org.jenkinsci.account.config;

public class MailConfig {

    private final String smtpServer;
    private final String smtpUser;
    private final String smtpPassword;
    private final boolean smtpAuth;
    private final int smtpPort;

    public MailConfig(String smtpServer, int smtpPort, String smtpUser, String smtpPassword, boolean smtpAuth) {
        this.smtpServer = smtpServer;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.smtpAuth = smtpAuth;
        this.smtpPort = smtpPort;
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

    public int getSmtpPort() {
        return smtpPort;
    }
}
