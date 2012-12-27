package org.jenkinsci.account.openid;

import org.jenkinsci.account.Myself;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.openid4java.association.AssociationException;
import org.openid4java.message.*;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerException;
import org.openid4java.server.ServerManager;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Session-scoped object that serves the top page.
 *
 * @author Kohsuke Kawaguchi
 */
public class Session {
    public final OpenIDServer provider;
    private final ServerManager manager;

    private ParameterList requestp;
    private final Set<String> approvedRealms = new HashSet<String>();
    private String mode;
    public String realm;
    public String returnTo;

    /**
     * OpenID URL of this user.
     */
    private String identity;
    public Myself myself;

    public Session(OpenIDServer provider) {
        this.provider = provider;
        this.manager = provider.manager;
    }

    /**
     * Binds client to URL.
     */
    public Client getClient() {
        return provider.client;
    }

    /**
     * Landing page for the OpenID protocol.
     */
    public HttpResponse doEntryPoint(StaplerRequest request) throws IOException {
        // these are the invariants during the whole conversation
        requestp = new ParameterList(request.getParameterMap());
        mode = requestp.getParameterValue("openid.mode");
        realm = requestp.getParameterValue("openid.realm");
        returnTo = requestp.getParameterValue("openid.return_to");

        if (realm==null && returnTo!=null)
            try {
                realm = new URL(returnTo).getHost();
            } catch (MalformedURLException e) {
                realm = returnTo; // fall back
            }

        return handleRequest();
    }

    private HttpResponse handleRequest() {
        try {
            if ("associate".equals(mode)) {
               // --- process an association request ---
                return new MessageResponse(manager.associationResponse(requestp));
            } else
            if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
                if (!isApproved()) {
                    // if the user hasn't logged in to us yet, this will make them do so
                    myself = provider.app.getMyself();
                    identity = provider.app.getUrl()+"~"+ myself.userId;

                    // let's confirm the user, which will take them to doVerify
                    return HttpResponses.forwardToView(this,"confirm");
                }

                Message rsp = manager.authResponse(requestp, identity, identity, true);
                respondToFetchRequest(rsp);
                if (rsp instanceof  AuthSuccess) {
                    // Need to sign after because SReg extension parameters are signed by openid4java
                    try {
                        manager.sign((AuthSuccess)rsp);
                    } catch (ServerException e) {
                        throw HttpResponses.error(500,e);
                    } catch (AssociationException e) {
                        throw HttpResponses.error(500,e);
                    }
                }

                return HttpResponses.redirectTo(rsp.getDestinationUrl(true));
            } else if ("check_authentication".equals(mode)) {
                return new MessageResponse(manager.verify(requestp));
            } else {
                throw HttpResponses.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"Unknown request: "+mode);
            }
        } catch (MessageException e) {
            e.printStackTrace();
            throw HttpResponses.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e);
        }
    }

    /**
     * Returns true if the login for the specified realm/return_to location is ACKed by the user.
     */
    private boolean isApproved() {
        if (approvedRealms.contains(realm)) return true;  // explicitly approved

        try {
            if (new URL(returnTo).getHost().endsWith(".jenkins-ci.org"))
                return true;    // apps in our own domains are trusted
        } catch (MalformedURLException e) {
            // fall through
        }

        return false;
    }

    /**
     * Responds to the fetch request by adding them.
     *
     * Java.net only gives us the ID, and everything else is just mechanically derived from it,
     * so there's no need to get the confirmation from users for passing them.
     */
    private void respondToFetchRequest(Message rsp) throws MessageException {
        AuthRequest authReq = AuthRequest.createAuthRequest(requestp, manager.getRealmVerifier());
        if (authReq.hasExtension(AxMessage.OPENID_NS_AX)) {
            MessageExtension ext = authReq.getExtension(AxMessage.OPENID_NS_AX);
            if (ext instanceof FetchRequest) {
                FetchRequest fetchReq = (FetchRequest) ext;
                FetchResponse fr = FetchResponse.createFetchResponse();

                for (Map.Entry<String,String> e : ((Map<String,String>)fetchReq.getAttributes()).entrySet()) {
//                    if (e.getValue().equals("http://axschema.org/contact/email")
//                    ||  e.getValue().equals("http://schema.openid.net/contact/email"))
//                        fr.addAttribute(e.getKey(),e.getValue(), myself.email);
                    if (e.getValue().equals("http://axschema.org/namePerson/friendly"))
                        fr.addAttribute(e.getKey(),e.getValue(), myself.userId);
                    if (e.getValue().equals("http://axschema.org/namePerson/first"))
                        fr.addAttribute(e.getKey(),e.getValue(), myself.firstName);
                    if (e.getValue().equals("http://axschema.org/namePerson/last"))
                        fr.addAttribute(e.getKey(),e.getValue(), myself.lastName);

                }

                rsp.addExtension(fr);
            }
        }
        if (authReq.hasExtension(SRegMessage.OPENID_NS_SREG)) {
            MessageExtension ext = authReq.getExtension(SRegMessage.OPENID_NS_SREG);
            if (ext instanceof SRegRequest) {
                SRegRequest req = (SRegRequest) ext;
                SRegResponse srsp = SRegResponse.createFetchResponse();

                for (String name : (List<String>)req.getAttributes()) {
                    if (name.equals("nickname"))
                        srsp.addAttribute(name,myself.userId);
                }

                rsp.addExtension(srsp);
            }
        }
    }

    public HttpResponse doVerify() {
        approvedRealms.add(realm);
        return handleRequest();
    }

    /**
     * Invalidates this session.
     */
    public void doLogout(StaplerRequest req) {
        req.getSession().invalidate();
    }
}