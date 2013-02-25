package org.jenkinsci.account;
import jiraldapsyncer.JiraLdapSyncer;
import jiraldapsyncer.ServiceLocator;
import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaResponse;
import org.jenkinsci.account.openid.JenkinsOpenIDServer;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.config.ConfigurationLoader;
import org.kohsuke.stopforumspam.Answer;
import org.kohsuke.stopforumspam.StopForumSpam;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
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
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static javax.naming.directory.DirContext.*;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Root of the account application.
 *
 * @author Kohsuke Kawaguchi
 */
public class Application {
    /**
     * Configuration parameter.
     */
    private final Parameters params;

    /**
     * For bringing the OpenID server into the URL space.
     */
    public final JenkinsOpenIDServer openid;

    public Application(Parameters params) throws IOException {
        this.params = params;
        this.openid = new JenkinsOpenIDServer(this);
    }

    public Application(Properties config) throws IOException {
        this(ConfigurationLoader.from(config).as(Parameters.class));
    }

    public Application(File config) throws IOException {
        this(ConfigurationLoader.from(config).as(Parameters.class));
    }

    public ReCaptcha createRecaptcha() {
        return ReCaptchaFactory.newSecureReCaptcha(params.recaptchaPublicKey(), params.recaptchaPrivateKey(), false);
    }

    public String getUrl() {
        return params.url();
    }

    /**
     * Receives the sign-up form submission.
     */
    public HttpResponse doDoSignup(
            StaplerRequest request,
            @QueryParameter String userid,
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,

            @Header("X-Forwarded-For") String ip    // client IP
    ) throws Exception {

        ReCaptcha reCaptcha = createRecaptcha();

        String challenge = request.getParameter("recaptcha_challenge_field");
        if (challenge==null)    throw new Error("challenge missing");
        String uresponse = request.getParameter("recaptcha_response_field");
        if (uresponse==null)    throw new Error("uresponse missing");
        ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(request.getRemoteAddr(), challenge, uresponse);

        if (!reCaptchaResponse.isValid()) {
            throw new UserError("Captcha mismatch. Please try again and retry a captcha to prove that you are a human");
        }

        userid = userid.toLowerCase();
        if (!VALID_ID.matcher(userid).matches())
            throw new UserError("Invalid user name: "+userid);

        if (isEmpty(firstName))
            throw new UserError("First name is required");
        if (isEmpty(lastName))
            throw new UserError("First name is required");
        if (isEmpty(email))
            throw new UserError("e-mail is required");

        // spam check
        for (Answer a : new StopForumSpam().build().ip(ip).email(email).query()) {
            if (a.isAppears() || email.equals("kk+spamtest@kohsuke.org")) {
                String text = String.format(
                        "Rejecting, likely spam: %s / ip=%s email=%s userId=%s lastName=%s firstName=%s",
                        a, ip, email, userid, lastName, firstName);
                LOGGER.warning(text);

                // send an e-mail to the admins
                Session s = createJavaMailSession();
                MimeMessage msg = new MimeMessage(s);
                msg.setSubject("Rejection of a new account creation");
                msg.setFrom(new InternetAddress("Admin <admin@jenkins-ci.org>"));
                msg.setRecipient(RecipientType.TO, new InternetAddress("jenkinsci-account-admins@googlegroups.com"));
                msg.setContent(
                        text+"\n\n"+
                        "To allow this account to be created, click the following link:\n"+
                        "https://jenkins-ci.org/account/admin/signup?userId="+enc(userid)+"&firstName="+enc(firstName)+"&lastName="+enc(lastName)+"&email="+enc(email)+"\n",
                        "text/plain");
                Transport.send(msg);

                throw new UserError("Due to the spam problem, we need additional verification for your sign-up request. Please contact jenkinsci-dev@googlegroups.com");
            }
        }

        String password = createRecord(userid, firstName, lastName, email);

        new User(userid,email).mailPassword(password);

        return new HttpRedirect("doneMail");
    }

    private static String enc(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s,"UTF-8");
    }

    /**
     * Adds the new user entry to LDAP.
     */
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
        } catch (NameAlreadyBoundException e) {
            throw new UserError("ID "+userid+" is already taken. Perhaps you already have an account imported from legacy java.net? You may try resetting the password.");
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

    /**
     * Handles the password reset form submission.
     */
    public HttpResponse doDoPasswordReset(@QueryParameter String id) throws Exception {
        final DirContext con = connect();
        try {
            Iterator<User> a = searchByWord(id, con);
            if (!a.hasNext())
                throw new UserError("No such user account found: "+id);

            User u = a.next();

            String p = PasswordUtil.generateRandomPassword();
            u.modifyPassword(con, p);
            u.mailPassword(p);
        } finally {
            con.close();
        }

        return new HttpRedirect("doneMail");
    }

    public User getUserById(String id, DirContext con) throws NamingException {
        String dn = "cn=" + id + "," + params.newUserBaseDN();
        return new User(con.getAttributes(dn));
    }

    /**
     * Object that represents some user in LDAP.
     */
    public class User {
        /**
         * User ID, such as 'kohsuke'
         */
        public final String id;
        /**
         * E-mail address.
         */
        public final String mail;

        public User(String id, String mail) {
            this.id = id;
            this.mail = mail;
        }

        public User(Attributes att) throws NamingException {
            id = (String) att.get("cn").get();
            mail = (String) att.get("mail").get();
        }

        public String getDn() {
            return String.format("cn=%s,%s", id, params.newUserBaseDN());
        }

        public void modifyPassword(DirContext con, String password) throws NamingException {
            con.modifyAttributes(getDn(),REPLACE_ATTRIBUTE,new BasicAttributes("userPassword",PasswordUtil.hashPassword(password)));
            LOGGER.info("User "+id+" reset the password: "+mail);
        }

        public void modifyEmail(DirContext con, String email) throws NamingException {
            con.modifyAttributes(getDn(),REPLACE_ATTRIBUTE,new BasicAttributes("mail",email));
            LOGGER.info("User "+id+" reset the e-mail address to: "+email);
        }

        /**
         * Sends a new password to this user.
         */
        public void mailPassword(String password) throws MessagingException {
            Session s = createJavaMailSession();
            MimeMessage msg = new MimeMessage(s);
            msg.setSubject("Your access to jenkins-ci.org");
            msg.setFrom(new InternetAddress("Admin <admin@jenkins-ci.org>"));
            msg.setRecipient(RecipientType.TO, new InternetAddress(mail));
            msg.setContent(
                    "Your userid is "+id+"\n"+
                    "Your temporary password is "+password+" \n"+
                    "\n"+
                    "Please visit http://jenkins-ci.org/account and update your password and profile\n",
                    "text/plain");
            Transport.send(msg);
        }

        public void delete(DirContext con) throws NamingException {
            con.destroySubcontext(getDn());
            LOGGER.info("User " + id + " deleted");
        }
    }

    private Session createJavaMailSession() {
        Properties props = new Properties(System.getProperties());
        props.put("mail.smtp.host",params.smtpServer());
        return Session.getInstance(props);
    }

    /**
     * Search LDAP with the given keyword, returning matching users.
     */
    public Iterator<User> searchByWord(String idOrMail, DirContext con) throws NamingException {
        final NamingEnumeration<SearchResult> e = con.search(params.newUserBaseDN(), "(|(mail={0})(cn={0}))", new Object[]{idOrMail}, new SearchControls());
        return new Iterator<User>() {
            public boolean hasNext() {
                return e.hasMoreElements();
            }

            public User next() {
                try {
                    return new User(e.nextElement().getAttributes());
                } catch (NamingException x) {
                    throw new RuntimeException(x);
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
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

    /**
     * Handles the login form submission.
     */
    public HttpResponse doDoLogin(
            @QueryParameter String userid,
            @QueryParameter String password,
            @QueryParameter String from
    ) throws Exception {
        if (userid==null || password==null)
            throw new UserError("Missing credential");

        String dn = "cn=" + userid + "," + params.newUserBaseDN();
        try {
            LdapContext context = connect(dn, password);    // make sure the password is valid
            try {
                Stapler.getCurrentRequest().getSession().setAttribute(Myself.class.getName(),
                        new Myself(this, dn, context.getAttributes(dn), getGroups(dn,context)));
            } finally {
                context.close();
            }
        } catch (AuthenticationException e) {
            throw new UserError(e.getMessage());
        }

        // to limit the redirect to this application, require that the from URL starts from '/'
        if (from==null || !from.startsWith("/")) from="/myself/";
        return HttpResponses.redirectTo(from);
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
        return current() !=null;
    }

    public boolean isAdmin() {
        Myself myself = current();
        return myself!=null && myself.isAdmin();        
    }

    /**
     * If the user has already logged in, retrieve the current user, otherwise
     * send the user to the login page.
     */
    public Myself getMyself() {
        Myself myself = current();
        if (myself==null) {
            // needs to login
            StaplerRequest req = Stapler.getCurrentRequest();
            StringBuilder from = new StringBuilder(req.getRequestURI());
            if (req.getQueryString()!=null)
                from.append('?').append(req.getQueryString());

            try {
                throw HttpResponses.redirectViaContextPath("login?from="+ URLEncoder.encode(from.toString(),"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }
        return myself;
    }

    /**
     * This is a test endpoint to make sure the reverse proxy forwarding is working.
     */
    public HttpResponse doForwardTest(@Header("X-Forwarded-For") String header) {
        return HttpResponses.plainText(header);
    }

    private Myself current() {
        return (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
    }

    public AdminUI getAdmin() {
        return getMyself().isAdmin() ? new AdminUI(this) : null;
    }

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_]+");
}
