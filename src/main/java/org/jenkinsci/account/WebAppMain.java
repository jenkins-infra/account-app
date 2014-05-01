package org.jenkinsci.account;

import org.kohsuke.stapler.framework.AbstractWebAppMain;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;

import java.io.File;

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
        File f;
        String con = System.getProperty("CONFIG");
        if (con!=null) {
            f = new File(con);
        } else {
            f = new File("config.properties");
        }
        return new Application(f);
    }
}
