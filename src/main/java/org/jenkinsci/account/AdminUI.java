package org.jenkinsci.account;

import org.jenkinsci.account.Application.User;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Root object of the admin UI.
 *
 * Only administrator gets access to this object.
 *
 * @author Kohsuke Kawaguchi
 */
public class AdminUI {
    private final Application app;

    public AdminUI(Application app) {
        this.app = app;
    }

    /**
     * Exposes the circuit breaker to UI.
     */
    public CircuitBreaker getCircuitBreaker() {
        return app.circuitBreaker;
    }

    public HttpResponse doSearch(@QueryParameter String word) throws NamingException {
        List<User> all = new ArrayList<User>();
        LdapContext con = app.connect();
        try {
            Iterator<User> itr = app.searchByWord(word, con);
            while (itr.hasNext())
                all.add(itr.next());

            return HttpResponses.forwardToView(this,"search.jelly").with("all",all);
        } finally {
            con.close();
        }
    }

    @RequirePOST
    public HttpResponse doPasswordReset(@QueryParameter String id, @QueryParameter String reason) throws NamingException, MessagingException {
        LdapContext con = app.connect();
        try {
            User u = app.getUserById(id, con);

            String p = PasswordUtil.generateRandomPassword();
            u.modifyPassword(con, p);
            u.mailPasswordReset(p, Stapler.getCurrentRequest().getRemoteUser(), reason);

            if (Myself.current().isAuthenticatedWithREST()) {
                return HttpResponses.ok();
            } else {
                return HttpResponses.forwardToView(this,"newPassword.jelly").with("user", u).with("password", p);
            }
        } finally {
            con.close();
        }
    }

    @RequirePOST
    public HttpResponse doEmailReset(@QueryParameter String id, @QueryParameter String email) throws NamingException {
        LdapContext con = app.connect();
        try {
            User u = app.getUserById(id, con);
            u.modifyEmail(con, email);
            return HttpResponses.redirectTo(".");
        } finally {
            con.close();
        }
    }

    @RequirePOST
    public HttpResponse doDelete(@QueryParameter String id, @QueryParameter String confirm) throws NamingException {
        if (!confirm.equalsIgnoreCase("YES"))
            return HttpResponses.error(HttpServletResponse.SC_BAD_REQUEST,"No confirmation given");

        LdapContext con = app.connect();
        try {
            User u = app.getUserById(id, con);

            u.delete(con);

            return HttpResponses.redirectTo(".");
        } finally {
            con.close();
        }
    }

    @RequirePOST
    public HttpResponse doDoSignup(
            @QueryParameter String userid,
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,
            @QueryParameter boolean skipPassword,
            @QueryParameter String message
    ) throws NamingException, MessagingException {
        String password = app.createRecord(userid, firstName, lastName, email);

        app.new User(userid,email).mailAccountCreated(!skipPassword, password, message);

        return HttpResponses.plainText("Created");
    }

    private static final Logger LOGGER = Logger.getLogger(AdminUI.class.getName());
}
