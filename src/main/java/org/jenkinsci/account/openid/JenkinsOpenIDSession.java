package org.jenkinsci.account.openid;

import org.jenkinsci.account.Myself;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.openid.server.*;
import org.kohsuke.stapler.openid.server.Session;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsOpenIDSession extends Session {
    private final Set<String> approvedRealms = new HashSet<String>();
    private final JenkinsOpenIDServer server;


    public JenkinsOpenIDSession(JenkinsOpenIDServer server) {
        super(server);
        this.server = server;
    }

    @Override
    protected HttpResponse authenticateUser(OpenIDIdentity id) {
        Myself myself = server.app.getMyself();

        id  .withFirstName(myself.firstName)
            .withLastName(myself.lastName)
            .withFullName(myself.firstName + ' ' + myself.lastName)
            .withNick(myself.userId);
        // not passing e-mail

        if (!isApproved()) {
            // let's confirm the user, which will take them to doVerify
            return HttpResponses.forwardToView(this, "confirm");
        }

        return null;
    }

    /**
     * Returns true if the login for the specified realm/return_to location is ACKed by the user.
     */
    private boolean isApproved() {
        if (approvedRealms.contains(getRealm())) return true;  // explicitly approved

        try {
            if (new URL(getReturnTo()).getHost().endsWith(".jenkins-ci.org"))
                return true;    // apps in our own domains are trusted
        } catch (MalformedURLException e) {
            // fall through
        }

        return false;
    }

    @RequirePOST
    public HttpResponse doVerify() {
        approvedRealms.add(getRealm());
        return handleRequest();
    }
}
