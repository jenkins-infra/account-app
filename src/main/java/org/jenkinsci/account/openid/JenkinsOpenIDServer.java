package org.jenkinsci.account.openid;

import org.jenkinsci.account.Application;
import org.kohsuke.stapler.openid.server.OpenIDServer;
import org.kohsuke.stapler.openid.server.Session;

import java.io.IOException;
import java.net.URL;

/**
 * OpenID server that allows users to use their Jenkins identity as an OpenID.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsOpenIDServer extends OpenIDServer {
    public final Application app;

    public JenkinsOpenIDServer(Application app) throws IOException {
        super(new URL(app.getUrl()+"openid/"));
        this.app = app;
    }

    @Override
    protected Session createSession() {
        return new JenkinsOpenIDSession(this);
    }
}
