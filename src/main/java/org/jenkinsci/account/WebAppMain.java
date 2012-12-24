package org.jenkinsci.account;

import org.kohsuke.stapler.framework.AbstractWebAppMain;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

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
        return new Application(new File("config.properties"));
    }
}
