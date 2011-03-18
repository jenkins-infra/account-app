package org.jenkinsci.account;

import org.jenkinsci.account.Application.User;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Root object of the admin UI.
 *
 * Only administrator gets access to this.
 *
 * @author Kohsuke Kawaguchi
 */
public class AdminUI {
    private final Application app;

    public AdminUI(Application app) {
        this.app = app;
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

    public HttpResponse doPasswordReset(@QueryParameter String id) throws NamingException {
        LdapContext con = app.connect();
        try {
            User u = app.getUserById(id, con);

            String p = PasswordUtil.generateRandomPassword();
            u.modifyPassword(con, p);

            return HttpResponses.forwardToView(this,"newPassword.jelly").with("user",u).with("password",p);
        } finally {
            con.close();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(AdminUI.class.getName());
}
