package org.jenkinsci.account.openid;

import org.jenkinsci.account.Application;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class OpenIDServer implements StaplerProxy {
    public final Application app;
    final ServerManager manager =new ServerManager();

    /**
     * The URL of this endpoint, like "http://foo:8080/"
     */
    public final String address;

    // test client
    public final Client client = new Client();

    public OpenIDServer(Application app, String address) {
        this.app = app;
        this.address = address;
        manager.setSharedAssociations(new InMemoryServerAssociationStore());
        manager.setPrivateAssociations(new InMemoryServerAssociationStore());
        manager.setOPEndpointUrl(address+"entryPoint");
    }

    public Session getTarget() {
        HttpSession hs = Stapler.getCurrentRequest().getSession();
        Session o = (Session) hs.getAttribute("session");
        if (o==null)
            hs.setAttribute("session",o=new Session(this));
        return o;
    }
}
