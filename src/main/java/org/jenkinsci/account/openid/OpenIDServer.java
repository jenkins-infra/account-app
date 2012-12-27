package org.jenkinsci.account.openid;

import org.jenkinsci.account.Application;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.openid4java.server.InMemoryServerAssociationStore;
import org.openid4java.server.ServerManager;

import javax.servlet.http.HttpSession;
import java.util.concurrent.TimeUnit;

/**
 * OpenID server that allows users to use their Jenkins identity as an OpenID.
 *
 * @author Kohsuke Kawaguchi
 */
public class OpenIDServer implements StaplerFallback {
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
        manager.setExpireIn((int)TimeUnit.DAYS.toSeconds(180));
    }

    public Session getStaplerFallback() {
        HttpSession hs = Stapler.getCurrentRequest().getSession();
        Session o = (Session) hs.getAttribute("session");
        if (o==null)
            hs.setAttribute("session",o=new Session(this));
        return o;
    }
}
