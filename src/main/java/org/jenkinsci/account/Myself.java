package org.jenkinsci.account;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.util.Set;
import java.util.logging.Logger;

import static org.jenkinsci.account.LdapAbuse.GITHUB_ID;
import static org.jenkinsci.account.LdapAbuse.SSH_KEYS;
import static org.jenkinsci.account.LdapAbuse.REGISTRATION_DATE;

/**
 * Represents the current user logged in and operations on it.
 *
 * @author Kohsuke Kawaguchi
 * @see Application#getMyself()
 */
public class Myself {
    private final Application parent;
    private final String dn;
    public String firstName, lastName, email, userId;
    public String githubId, sshKeys, registrationDate;
    private final Set<String> groups;

    public Myself(Application parent, String dn, Attributes attributes, Set<String> groups) throws NamingException {
        this(parent, dn,
            getAttribute(attributes,"givenName"),
            getAttribute(attributes,"sn"),
            getAttribute(attributes,"mail"),
            getAttribute(attributes,"cn"),
            getAttribute(attributes, REGISTRATION_DATE),
            getAttribute(attributes, GITHUB_ID),
            getAttribute(attributes, SSH_KEYS),
            groups);
    }

    public Myself(Application parent, String dn, String firstName, String lastName, String email, String userId, String registrationDate, String githubId, String sshKeys, Set<String> groups) {
        this.parent = parent;
        this.dn = dn;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.userId = userId;
        this.githubId = githubId;
        this.sshKeys = sshKeys;
        this.groups = groups;
        this.registrationDate = registrationDate;
    }

    public static Myself current() {
        return (Myself) Stapler.getCurrentRequest().getSession().getAttribute(Myself.class.getName());
    }

    /**
     * Is this an admin user?
     */
    public boolean isAdmin() {
        return groups.contains("admins");
    }

    private static String getAttribute(Attributes attributes, String name) throws NamingException {
        Attribute att = attributes.get(name);
        return att!=null ? (String) att.get() : null;
    }

    public HttpResponse doUpdate(
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,
            @QueryParameter String githubId,
            @QueryParameter String sshKeys,
            @QueryParameter String password,
            @QueryParameter String newPassword1,
            @QueryParameter String newPassword2
    ) throws Exception {

        final Attributes attrs = new BasicAttributes();

        attrs.put("givenName", fixEmpty(firstName));
        attrs.put("sn", fixEmpty(lastName));
        attrs.put("mail", email);
        attrs.put(GITHUB_ID,fixEmpty(githubId));
        attrs.put(SSH_KEYS,fixEmpty(sshKeys)); // hack since I find it too hard to add custom attributes to LDAP

        LdapContext context = parent.connect();
        try {
            context.modifyAttributes(dn,DirContext.REPLACE_ATTRIBUTE,attrs);
        } finally {
            context.close();
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.githubId = githubId;
        this.sshKeys = sshKeys;

        LOGGER.info("User "+userId+" updated the profile. email="+email);

        if (fixEmpty(newPassword1)!=null) {
            doChangePassword(password,newPassword1,newPassword2);
        }

        return new HttpRedirect("done");
    }

    // no longer invoked directly from outside, but left as is
    public HttpResponse doChangePassword(
            @QueryParameter String password,
            @QueryParameter String newPassword1,
            @QueryParameter String newPassword2
    ) throws Exception {

        if (!newPassword1.equals(newPassword2))
            throw new Error("Password mismatch");

        // verify the current password
        parent.connect(dn,password).close();

        // then update
        Attributes attrs = new BasicAttributes();
        attrs.put("userPassword", PasswordUtil.hashPassword(newPassword1));

        LdapContext context = parent.connect();
        try {
            context.modifyAttributes(dn,DirContext.REPLACE_ATTRIBUTE,attrs);
        } finally {
            context.close();
        }

        LOGGER.info("User "+userId+" changed the password");

        return new HttpRedirect("done");
    }

    private String fixEmpty(String s) {
        if (s!=null && s.length()==0)   return null;
        return s;
    }

    private static final Logger LOGGER = Logger.getLogger(Myself.class.getName());
}
