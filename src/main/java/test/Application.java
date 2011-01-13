package test; 
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.config.ConfigurationProxy;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import static javax.naming.directory.DirContext.*;
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
            @QueryParameter String email
    ) throws Exception {


        Attributes attrs = new BasicAttributes();
        attrs.put("objectClass", "inetOrgPerson");
        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);
        String password = generateRandomPassword();
        attrs.put("userPassword", hashPassword(password));

        final DirContext con = connect();
        try {
            con.createSubcontext("cn="+userid+","+params.newUserBaseDN(), attrs);
        } finally {
            con.close();
        }

        LOGGER.info("User "+userid+" signed up: "+email);

        mailPassword(email,userid,password);
        
        return new HttpRedirect("doneMail");
    }

    public HttpResponse doDoPasswordReset(@QueryParameter String id) throws Exception {
        final DirContext con = connect();
        try {
            NamingEnumeration<SearchResult> a = con.search(params.newUserBaseDN(), "(|(mail={0})(cn={0}))", new Object[]{id}, new SearchControls());
            if (!a.hasMore())
                throw new Error("No such user account found: "+id);

            SearchResult r = a.nextElement();
            Attributes att = r.getAttributes();

            String p = PasswordUtil.generateRandomPassword();
            String dn = r.getName()+","+params.newUserBaseDN();
            con.modifyAttributes(dn,REPLACE_ATTRIBUTE,new BasicAttributes("userPassword",PasswordUtil.hashPassword(p)));

            String userid = (String) att.get("cn").get();
            String mail = (String) att.get("mail").get();
            LOGGER.info("User "+userid+" reset the password: "+mail);
            mailPassword(mail, userid, p);
        } finally {
            con.close();
        }

        return new HttpRedirect("doneMail");
    }

    private void mailPassword(String to, String cn, String password) throws MessagingException {
        Properties props = new Properties(System.getProperties());
        props.put("mail.smtp.host",params.smtpServer());
        Session s = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(s);
        msg.setSubject("Your access to jenkins-ci.org");
        msg.setFrom(new InternetAddress("Admin <admin@jenkins-ci.org>"));
        msg.setRecipient(RecipientType.TO, new InternetAddress(to));
        msg.setContent(
                "Your userid is "+cn+"\n"+
                "Your password is "+password, "text/plain");
        Transport.send(msg);
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
        Myself myself = (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
        if (myself==null)   // needs to login
            throw HttpResponses.redirectViaContextPath("login");
        return myself;
    }

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());
}
