package org.jenkinsci.account;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jenkinsci.account.config.LdapConfig;
import org.jenkinsci.account.config.MailConfig;
import org.kohsuke.stapler.framework.AbstractWebAppMain;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;

/**
 * Bootstrap code.
 *
 * @author Kohsuke Kawaguchi
 */
public class WebAppMain extends AbstractWebAppMain<Application> {
    public WebAppMain() {
        super(Application.class);
        DefaultScriptInvoker.COMPRESS_BY_DEFAULT = false;   // blind shot
    }

    @Override
    protected String getApplicationName() {
        return "APP";
    }

    @Override
    public Application createApplication() throws Exception {
        Config conf = ConfigFactory.load();

        LdapConfig ldapConfig = new LdapConfig(
                conf.getString("ldap.server"),
                conf.getString("ldap.managerDN"),
                conf.getString("ldap.managerPassword"),
                conf.getString("ldap.newUserBaseDN")
        );

        MailConfig mailConfig = new MailConfig(
                conf.getString("mail.server"),
                conf.getString("mail.sender"),
                conf.getInt("mail.port"),
                conf.hasPath("mail.user") ? conf.getString("mail.user") : null,
                conf.hasPath("mail.password") ? conf.getString("mail.password") : null,
                conf.getBoolean("mail.useAuth")
        );

        Parameters parameters = new Parameters(
                conf.getString("url"),
                ldapConfig,
                mailConfig,
                conf.hasPath("circuitBreakerFile") ? conf.getString("circuitBreakerFile") : null
        );

        return new Application(parameters);
    }
}
