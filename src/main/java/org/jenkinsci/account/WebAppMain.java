package org.jenkinsci.account;

import org.kohsuke.stapler.framework.AbstractWebAppMain;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author Kohsuke Kawaguchi
 */
public class WebAppMain extends AbstractWebAppMain<Application> {
    public WebAppMain() {
        super(Application.class);
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
