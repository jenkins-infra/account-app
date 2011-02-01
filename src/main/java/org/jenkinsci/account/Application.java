package org.jenkinsci.account;
import jiraldapsyncer.JiraLdapSyncer;
import jiraldapsyncer.ServiceLocator;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;
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
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static javax.naming.directory.DirContext.*;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

/**
 * @author Kohsuke Kawaguchi
 */
public class Application {
    private final Parameters params;
//    public final File rootDir;

    public Application(Parameters params) {
        this.params = params;
    }

    public Application(Properties config) {
        this(ConfigurationProxy.create(config, Parameters.class));
    }

    public Application(File config) throws IOException {
        this(ConfigurationProxy.create(config, Parameters.class));
    }

    public ReCaptcha createRecaptcha() {
        return ReCaptchaFactory.newReCaptcha(params.recaptchaPublicKey(), params.recaptchaPrivateKey(), false);
    }

    public HttpResponse doDoSignup(
            StaplerRequest request,
            @QueryParameter String userid,
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email
    ) throws Exception {

        ReCaptcha reCaptcha = createRecaptcha();

        String challenge = request.getParameter("recaptcha_challenge_field");
        String uresponse = request.getParameter("recaptcha_response_field");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(request.getRemoteAddr(), challenge, uresponse);

        if (!reCaptchaResponse.isValid()) {
            throw new Error("Captcha mismatch");
        }

        userid = userid.toLowerCase();
        if (!VALID_ID.matcher(userid).matches())
            throw new Error("Invalid user name: "+userid);


        String password = createRecord(userid, firstName, lastName, email);

        mailPassword(email,userid,password);
        
        return new HttpRedirect("doneMail");
    }

    public String createRecord(String userid, String firstName, String lastName, String email) throws NamingException {
        Attributes attrs = new BasicAttributes();
        attrs.put("objectClass", "inetOrgPerson");
        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);
        String password = PasswordUtil.generateRandomPassword();
        attrs.put("userPassword", PasswordUtil.hashPassword(password));

        final DirContext con = connect();
        try {
            String fullDN = "cn=" + userid + "," + params.newUserBaseDN();
            con.createSubcontext(fullDN, attrs).close();

            // add to the right group
            try {
                con.modifyAttributes("cn=all,ou=groups,dc=jenkins-ci,dc=org",ADD_ATTRIBUTE,new BasicAttributes("member",fullDN));
            } catch (AttributeInUseException e) {
                // deletes and re-add it to make the case match
                con.modifyAttributes("cn=all,ou=groups,dc=jenkins-ci,dc=org",REMOVE_ATTRIBUTE,new BasicAttributes("member",fullDN));
                con.modifyAttributes("cn=all,ou=groups,dc=jenkins-ci,dc=org",ADD_ATTRIBUTE,new BasicAttributes("member",fullDN));
            }
        } finally {
            con.close();
        }

        try {
            JiraLdapSyncer jiraLdapSyncer = (JiraLdapSyncer) new ServiceLocator().lookupService(JiraLdapSyncer.ROLE);
            jiraLdapSyncer.syncOneUserFromLDAPToJIRA(userid);
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Failed to register " + userid + " to JIRA", e);
        }

        LOGGER.info("User "+userid+" signed up: "+email);
        return password;
    }

    public HttpResponse doDoPasswordReset(@QueryParameter String id) throws Exception {
        final DirContext con = connect();
        try {
            NamingEnumeration<SearchResult> a = con.search(params.newUserBaseDN(), "(|(mail={0})(cn={0}))", new Object[]{id}, new SearchControls());
            if (!a.hasMore())
                throw new UserError("No such user account found: "+id);

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
                "Your temporary password is "+password+"\n"+
                "\n"+
                "Please visit http://jenkins-ci.org/account and update your password and profile\n",
                "text/plain");
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
        if (userid==null || password==null)
            throw new UserError("Missing credential");

        String dn = "cn=" + userid + "," + params.newUserBaseDN();
        try {
            LdapContext context = connect(dn, password);    // make sure the password is valid
            try {
                Stapler.getCurrentRequest().getSession().setAttribute(Myself.class.getName(),
                        new Myself(this,dn, context.getAttributes(dn), getGroups(dn,context)));
            } finally {
                context.close();
            }
        } catch (AuthenticationException e) {
            throw new UserError(e.getMessage());
        }
        return new HttpRedirect("myself/");
    }

    /**
     * Obtains the group of the user specified by the given DN.
     */
    Set<String> getGroups(String dn, LdapContext context) throws NamingException {
        Set<String> groups = new HashSet<String>();
        SearchControls c = new SearchControls();
        c.setReturningAttributes(new String[]{"cn"});
        c.setSearchScope(SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> e = context.search("dc=jenkins-ci,dc=org", "(& (objectClass=groupOfNames) (member={0}))", new Object[]{dn}, c);
        while (e.hasMore()) {
            groups.add(e.nextElement().getAttributes().get("cn").get().toString());
        }
        return groups;
    }

    public HttpResponse doLogout(StaplerRequest req) {
        req.getSession().invalidate();
        return HttpResponses.redirectToDot();
    }

    public boolean isLoggedIn() {
        Myself myself = (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
        return myself!=null;
    }

    public Myself getMyself() {
        Myself myself = (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
        if (myself==null)   // needs to login
            throw HttpResponses.redirectViaContextPath("login");
        return myself;
    }

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_]+");
}
