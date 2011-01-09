package test;

import org.apache.commons.configuration.Configuration;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.config.ConfigurationProxy;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Properties;

import static test.PasswordUtil.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class Application {
    private final Parameters params;

    public Application(Parameters params) {
        this.params = params;
    }

    public Application(Properties config) {
        this.params = ConfigurationProxy.create(config,Parameters.class);
    }

    public HttpResponse doDoSignup(
            @QueryParameter String userid,
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,
            @QueryParameter String password1,
            @QueryParameter String password2
    ) throws Exception {


        if (!password1.equals(password2))
            throw new Error("Password mismatch");

        Attributes attrs = new BasicAttributes();
        attrs.put("objectClass", "inetOrgPerson");
        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);
        attrs.put("userPassword", hashPassword(password1));

        final DirContext con = connect();
        try {
            con.createSubcontext("cn="+userid+","+params.newUserBaseDN(), attrs);
        } finally {
            con.close();
        }
        
        return new HttpRedirect("done");
    }

    public LdapContext connect() throws NamingException {
        return connect(params.managerDN(), params.managerPassword());
    }

    public LdapContext connect(String dn, String password) throws NamingException {
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, params.server());
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(env, null);
    }

    public HttpResponse doDoLogin(
            @QueryParameter String userid,
            @QueryParameter String password
    ) throws Exception {

        String dn = "cn=" + userid + "," + params.newUserBaseDN();
        LdapContext context = connect(dn, password);    // make sure the password is valid
        try {
            Stapler.getCurrentRequest().getSession().setAttribute(Myself.class.getName(),
                    new Myself(this,dn, context.getAttributes(dn)));
        } finally {
            context.close();
        }
        return new HttpRedirect("myself/");
    }

    public HttpResponse doLogout(StaplerRequest req) {
        req.getSession().invalidate();
        return HttpResponses.redirectToDot();
    }

    public Myself getMyself() {
        return (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
    }
}
