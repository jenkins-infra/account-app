package org.jenkinsci.account;
import jiraldapsyncer.JiraLdapSyncer;
import jiraldapsyncer.ServiceLocator;
import org.jenkinsci.account.openid.JenkinsOpenIDServer;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.config.ConfigurationLoader;
import org.kohsuke.stopforumspam.Answer;
import org.kohsuke.stopforumspam.StopForumSpam;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
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
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static javax.naming.directory.DirContext.*;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.jenkinsci.account.LdapAbuse.*;

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

    // not exposing this to UI
    /*package*/ final CircuitBreaker circuitBreaker;

    public Application(Parameters params) throws IOException {
        this.params = params;
        this.openid = new JenkinsOpenIDServer(this);
        this.circuitBreaker = new CircuitBreaker(params);
    }

    public Application(Properties config) throws IOException {
        this(ConfigurationLoader.from(config).as(Parameters.class));
    }

    public Application(File config) throws IOException {
        this(ConfigurationLoader.from(config).as(Parameters.class));
    }

    public String captchaPublicKey() {
        return params.recaptchaPublicKey();
    }

    public String getUrl() {
        return params.url();
    }

    /**
     * Receives the sign-up form submission.
     */
    public HttpResponse doDoSignup(
            StaplerRequest request,
            StaplerResponse response,
            @QueryParameter String userid,
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,
            @QueryParameter String usedFor,
            @Header("X-Forwarded-For") String ip    // client IP
    ) throws Exception {

        ip = extractFirst(ip);

        String uresponse = request.getParameter("g-recaptcha-response");
        if (uresponse==null)    throw new Error("captcha missing");

        if (!verifyCaptcha(uresponse, ip)) {
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

        if(checkCookie(request, ALREADY_SIGNED_UP)) {
//            return maybeSpammer(userid, firstName, lastName, email, ip, "Cookie");
            throw new UserError(SPAM_MESSAGE);
        }

        if(Pattern.matches("^jb\\d+@gmail.com", email)) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Email blacklist");
        }

        for (String fragment : USERID_BLACKLIST) {
            if(userid.contains(fragment)) {
                return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Userid Blacklist");
            }
        }

        for (String fragment : IP_BLACKLIST) {
            if(ip.startsWith(fragment)) {
                return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "IP Blacklist");
            }
        }
        // domain black list
        String lm = email.toLowerCase(Locale.ENGLISH);
        for (String fragment : EMAIL_BLACKLIST) {
            if (lm.contains(fragment))
                return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Blacklist");
        }

        for(String fragment : USE_BLACKLIST) {
            if(usedFor != null && usedFor.trim().isEmpty()) {
                if (usedFor.trim().equalsIgnoreCase(fragment)) {
                    return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Blacklisted Use");
                }
            }
        }

        if(badNameElement(usedFor)) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Garbled Use");
        }

        if(userid.equalsIgnoreCase(usedFor) || firstName.equalsIgnoreCase(usedFor) || lastName.equalsIgnoreCase(usedFor) || email.equalsIgnoreCase(usedFor)) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "use same as name");
        }

        if(badNameElement(userid) || badNameElement(firstName) || badNameElement(lastName)) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "bad name element");
        }

        final DirContext con = connect();
        try {
            ldapObjectExists(con, "(id={0})", userid, "ID " + userid + " is already taken. Perhaps you already have an account imported from legacy java.net? You may try resetting the password.");
        } finally {
            con.close();
        }

        if(circuitBreaker.check()) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "circuitBreaker");
        }

        // spam check
        for (Answer a : new StopForumSpam().build().ip(ip).email(email).query()) {
            if (a.isAppears()) {
                return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, a.toString());
            }
        }

        Cookie cookie = new Cookie(ALREADY_SIGNED_UP, "1");
        cookie.setDomain("jenkins-ci.org");
        cookie.setPath("/account");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        try {
            String password = createRecord(userid, firstName, lastName, email);
            LOGGER.info("User "+userid+" is from "+ip);

            new User(userid,email).mailPassword(password);
        } catch (UserError ex) {
            return maybeSpammer(userid, firstName, lastName, email, ip, usedFor, "Existing email in system");
        }

        return new HttpRedirect("doneMail");
    }

    private boolean badNameElement(String userid) {
        return Pattern.matches("^[sdfghrt0-9]+$", userid.toLowerCase());
    }

    public String geoIp(String ip) {

        try {
            URL url = new URL("http://freegeoip.net/csv/" + ip);
            BufferedReader reader = new BufferedReader( new InputStreamReader(url.openStream()));
            return reader.readLine();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private boolean verifyCaptcha(String uresponse, String ip) {
        String postParams = "secret=" + URLEncoder.encode(params.recaptchaPrivateKey()) +
                           "&remoteip=" + URLEncoder.encode(ip) +
                           "&response=" + URLEncoder.encode(uresponse);
        try {
            URL obj = new URL("https://www.google.com/recaptcha/api/siteverify");
            HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //parse JSON response and return 'success' value
            JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();
            return jsonObject.getBoolean("success");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkCookie(StaplerRequest request, String x) {
        for (Cookie cookie: request.getCookies()) {
            if(cookie.getName().equals(ALREADY_SIGNED_UP)) {
                return "1".equals(cookie.getValue());
            }
        }
        return false;
    }

    private HttpResponse maybeSpammer(String userid, String firstName, String lastName, String email, String ip, String usedFor, String blockReason) throws MessagingException, UnsupportedEncodingException {
        String text = String.format(
                "Rejecting, likely spam: %s / ip=%s email=%s userId=%s lastName=%s firstName=%s\nuse=%s",
                blockReason, ip, email, userid, lastName, firstName, usedFor);
        LOGGER.warning(text);

        // send an e-mail to the admins
        Session s = createJavaMailSession();
        MimeMessage msg = new MimeMessage(s);
        msg.setSubject("Rejection of a new account creation for " + firstName + " " + lastName);
        msg.setFrom(new InternetAddress("Admin <admin@jenkins-ci.org>"));
        msg.setRecipient(RecipientType.TO, new InternetAddress("jenkinsci-account-admins@googlegroups.com"));
        msg.setContent(
                text+"\n\n"+
                    "GeoIP info: " + geoIp(ip) +"\n\n" +
                "To allow this account to be created, click the following link:\n"+
                "https://jenkins-ci.org/account/admin/signup?userId="+enc(userid)+"&firstName="+enc(firstName)+"&lastName="+enc(lastName)+"&email="+enc(email)+"\n",
                "text/plain");
        Transport.send(msg);

        throw new UserError(SPAM_MESSAGE);
    }

    /**
     * If IP consists of multiple tokens, like "1.2.3.4, 5.6.7.8" then just extract the first one.
     */
    private String extractFirst(String ip) {
        if (ip==null)   return "127.0.0.1";
        int idx = ip.indexOf(",");
        if (idx>0)  return ip.substring(0,idx);
        return ip;
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
        attrs.put(REGISTRATION_DATE, new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        attrs.put(SENIOR_STATUS, "N");

        final DirContext con = connect();
        try {

            ldapObjectExists(con, "(id={0})", userid, "ID " + userid + " is already taken. Perhaps you already have an account imported from legacy java.net? You may try resetting the password.");
            ldapObjectExists(con, "(mail={0})", email, SPAM_MESSAGE);

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

    private void ldapObjectExists(DirContext con, String filterExpr, Object filterArgs, String message) throws NamingException {
        final NamingEnumeration<SearchResult> userSearch = con.search(params.newUserBaseDN(), filterExpr, new Object[]{filterArgs}, new SearchControls());
        if (userSearch.hasMore()) {
            throw new UserError(message);
        }
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

    public static final List<String> EMAIL_BLACKLIST = Arrays.asList(
        "@anappthat.com",
        "@clrmail.com",
        "@dodsi.com",
        "@grandmamail.com",
        "@grandmasmail.com",
        "@guerrillamail.com",
        "@mailcatch.com",
        "@maildx.com",
        "@mailinator.com",
        "@mailnesia.com",
        "@sharklasers.com",
        "@thrma.com",
        "@webtrackker.com",
        "@yahoo.co.id",
        "@yopmail.com",
        "@zetmail.com",
        "abdheshnir.vipra@gmail.com",
        "adreahilton@gmail.com",
        "ajayrudelee@gmail.com",
        "ajymaansingh@gmail.com",
        "AndentspouRita@gmail.com",
        "andorclifs@gmail.com",
        "angthpofphilip@gmail.com",
        "anilkandpal0@gmail.com",
        "anilsingh7885945@gmail.com",
        "ankit",
        "ashishkumar",
        "ashwanikumar",
        "bcmdsbncskj@yandex.com",
        "bidupan12@gmail.com",
        "billydoch021@gmail.com",
        "ciodsjiocxjosa@yandex.com",
        "cooperdavidd@gmail.com",
        "crsgroupindia@gmail.com",
        "dasdasdsas32@gmail.com",
        "deepakkumar02singh@gmail.com",
        "dersttycert101@gmail.com",
        "donallakarpissaa@gmail.com",
        "drruytuyj@gmail.com",
        "folk.zin87@gmail.com",
        "gamblerbhaijaan@gmail.com",
        "georgegallego.com@gmail.com",
        "georgiaaby@gmail.com",
        "Hauptnuo214@gmail.com",
        "hcuiodsciodso@yandex.com",
        "HenryMullins",
        "hontpojpatricia",
        "HounchpowJohn@gmail.com",
        "HowerpofHarold@gmail.com",
        "hrrbanga",
        "hsharish",
        "huin.lisko097@gmail.com",
        "intelomedia02@gmail.com",
        "intuitphonenumber",
        "iqinfotech",
        "janes6521@gmail.com",
        "janessmith",
        "jayshown81@gmail.com",
        "jksadnhk@gmail.com",
        "johngarry227@gmail.com",
        "johnmaclan1@gmail.com",
        "johnmatty55@gmail.com",
        "JohnnyColvin428@gmail.com",
        "johnseo130@gmail.com",
        "kalidass34212@gmail.com",
        "kk+spamtest@kohsuke.org",
        "kripalsingh446@gmail.com",
        "krishgail30@yahoo.com",
        "kumar.uma420@gmail.com",
        "kumarprem",
        "kumarsujit",
        "LarrySilva",
        "litawilliam36@gmail.com",
        "loksabha100@gmail.com",
        "macden",
        "maohinseeeeeee@outlook.com",
        "masmartin71@gmail.com",
        "mmmarsh12@gmail.com",
        "mohandaerer",
        "mohankeeded",
        "morrisonjohn293@gmail.com",
        "ncrpoo",
        "ncrsona",
        "nishanoor32",
        "obat@",
        "OllouretypotIda@gmail.com",
        "omprakash",
        "pankaj",
        "paroccepoytamarac@gmail.com",
        "paulseanseo91@gmail.com",
        "pintu",
        "pogogames483@gmail.com",
        "poonamkamalpatel@gmail.com",
        "porterquines@gmail.com",
        "pranay4job@gmail.com",
        "printerhelplinenumber@gmail.com",
        "priturpocdickr@gmail.com",
        "quickbook",
        "r.onysokha@gmail.com",
        "rahul4cool2003@gmail.com",
        "rajdsky7@gmail.com",
        "rehel55rk@gmail.com",
        "righttechnical",
        "rohitsona121090@gmail.com",
        "sandysharmja121@gmail.com",
        "sandysharmja121@gmail.com",
        "seo01@gmail.com",
        "seo02@gmail.com",
        "seo03@gmail.com",
        "seosupport",
        "seoxpertchandan@gmail.com",
        "skprajapaty@gmail.com",
        "smartsolution3000@gmail.com",
        "smithmartin919@gmail.com",
        "snjbisth8@gmail.com",
        "spyvikash",
        "stephanflorian1@gmail.com",
        "stybesto13",
        "sundeepsa123@gmail.com",
        "sunflowerrosy@outlook.com",
        "sunilkundujat@gmail.com",
        "sunjara10@gmail.com",
        "Sweenypar210@gmail.com",
        "thjyt@yandex.com",
        "tomcopper6@gmail.com",
        "toren55rk@gmail.com",
        "viz.michel@gmail.com",
        "vr4vikasrastogi@gmail.com",
        "watpad",
        "webdevelopera@gmail.com",
        "win.tech",
        "yadavqs@gmail.com",
        "zozojams11@gmail.com"
    );

    public static final List<String> IP_BLACKLIST = Arrays.asList(
        "1.186.172.",
        "1.187.118.175",
        "1.187.126.76",
        "1.187.162.39",
        "1.22.39.244",
        "1.39.101.93",
        "1.39.34.181",
        "1.39.50.144",
        "1.39.51.63",
        "101.59.76.223",
        "101.60.",
        "101.63.200.188",
        "103.10.197.194",
        "103.19.153.130",
        "103.192.64.",
        "103.192.65.",
        "103.204.168.18",
        "103.226.202.171",
        "103.226.202.211",
        "103.245.118.",
        "103.254.154.229",
        "103.44.18.221",
        "103.55.",
        "106.67.113.167",
        "106.76.167.41",
        "110.172.140.98",
        "110.227.181.55",
        "110.227.183.246",
        "111.93.63.62",
        "112.196.160.122",
        "114.143.173.139",
        "115.160.250.34",
        "115.184.",
        "116.202.36.",
        "116.203.",
        "117.198.",
        "119.81.230.137",
        "119.82.95.142",
        "120.57.17.65",
        "120.57.86.248",
        "120.59.205.205",
        "121.242.77.200",
        "122.162.88.67",
        "122.169.130.19",
        "122.173.",
        "122.175.221.219",
        "122.177.",
        "122.180.",
        "123.136.209.119",
        "123.254.107.229",
        "124.41.241.203",
        "125.16.2.102",
        "125.63.107.204",
        "138.128.180.",
        "14.141.148.206",
        "14.141.51.5",
        "14.96.",
        "14.98.",
        "171.48.38.188",
        "171.50.146.100",
        "180.151.",
        "182.156.72.162",
        "182.64.",
        "182.68.",
        "182.69.",
        "182.73.182.170",
        "182.75.144.58",
        "202.53.94.4",
        "202.91.134.66",
        "202.91.76.82",
        "203.122.41.130",
        "203.122.7.236",
        "223.225.42.57",
        "27.56.47.65",
        "27.60.131.203",
        "27.7.210.21",
        "27.7.213.175",
        "43.230.198.228",
        "43.230.198.9",
        "43.245.149.107",
        "43.251.84.",
        "43.252.27.52",
        "43.252.29.202",
        "43.252.33.70",
        "43.245.151.156",
        "45.115.",
        "45.120.56.65",
        "45.121.188.46",
        "45.121.189.236",
        "45.122.123.47",
        "45.127.42.63",
        "45.55.3.174",
        "45.56.154.150",
        "49.15.149.23",
        "49.156.150.242",
        "49.204.252.214",
        "49.244.214.39",
        "59.180.132.51",
        "59.180.25.215",
        "61.12.72.244",
        "61.12.72.246",
        "62.210.139.80" // proxy? twice an Indian spammer jumped to this IP
    );

    public static final List<String> USE_BLACKLIST = Arrays.asList(
        "ad",
        "add a page",
        "add page",
        "admin",
        "admins",
        "advertisement",
        "advertising",
        "article",
        "articles",
        "asdf",
        "blog submit",
        "blog user",
        "blog",
        "bloging",
        "business",
        "businessman",
        "bussiness",
        "capturing",
        "content marketing",
        "creating",
        "discussion",
        "donation",
        "edit page",
        "edit",
        "for using wiki and jira",
        "for wiki and jira use",
        "forum post",
        "forum",
        "Fun Wiki",
        "funtime",
        "game",
        "get informaion",
        "good",
        "google",
        "group discussion",
        "help support",
        "helpline and support",
        "helpline",
        "https://jenkins-ci.org/account/signup",
        "information",
        "internet",
        "jira",
        "keyword promotion",
        "knowledge",
        "learn",
        "love",
        "marketing",
        "material",
        "meet jenkins",
        "networking",
        "news update",
        "news",
        "no",
        "nothing",
        "office",
        "other",
        "page",
        "post profile",
        "post",
        "posting",
        "pratice",
        "problem solved",
        "profile",
        "promotion",
        "publish",
        "question",
        "read and write",
        "read",
        "reading",
        "robot",
        "seo",
        "share info",
        "share post",
        "sharing",
        "social",
        "solve problem",
        "spam",
        "spread information",
        "student",
        "studies",
        "study",
        "submit page",
        "support",
        "surfing",
        "teacher",
        "tech support",
        "technical help support",
        "technical help",
        "technical support",
        "tutorial",
        "tutorials",
        "useful",
        "want to study",
        "web page",
        "website",
        "what",
        "wiki page",
        "wiki submission",
        "wiki",
        "yes"
    );

    public static final List<String> USERID_BLACKLIST = Arrays.asList(
        "intuitphonenumber",
        "jamesbond",
        "larrysilva",
        "quickbook",
        "watpad"
    );

    public static final String SPAM_MESSAGE = "Due to the spam problem, we will need additional verification for your sign-up request. " +
            "More details are found on <a href='https://wiki.jenkins-ci.org/display/JENKINS/User+Account+on+Jenkins'>our wiki</a> on how to get your account created.";

    // Somewhat cryptic name for cookie, so prying eyes don't know its use.
    public static final String ALREADY_SIGNED_UP = "JENKINSACCOUNT";
}
