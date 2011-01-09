package test;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConnectionFactory {
    private final String server;
    private final String dn;
    private final String password;

    public ConnectionFactory(String server, String dn, String password) {
        this.server = server;
        this.dn = dn;
        this.password = password;
    }

    public LdapContext connect() throws NamingException {
        Hashtable<String,String> env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, server);
        env.put(Context.SECURITY_PRINCIPAL, dn);
        env.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(env, null);
    }
}
