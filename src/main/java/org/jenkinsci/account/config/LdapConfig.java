package org.jenkinsci.account.config;

public class LdapConfig {

    private final String server;
    private final String managerDn;
    private final String managerPassword;
    private final String newUserBaseDn;

    public LdapConfig(String server, String managerDn, String managerPassword, String newUserBaseDn) {
        this.server = server;
        this.managerDn = managerDn;
        this.managerPassword = managerPassword;
        this.newUserBaseDn = newUserBaseDn;
    }

    public String getNewUserBaseDn() {
        return newUserBaseDn;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public String getServer() {
        return server;
    }
}
