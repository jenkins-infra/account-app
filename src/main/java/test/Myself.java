package test;

import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class Myself {
    private final Application parent;
    private final String dn;
    public String firstName, lastName, email, userId;
    public String githubId, sshKeys;

    public Myself(Application parent, String dn, Attributes attributes) throws NamingException {
        this.parent = parent;
        this.dn = dn;

        firstName = getAttribute(attributes,"givenName");
        lastName = getAttribute(attributes,"sn");
        email = getAttribute(attributes,"mail");
        userId = getAttribute(attributes,"cn");
        githubId = getAttribute(attributes,"employeeNumber");
        sshKeys = getAttribute(attributes,"preferredLanguage");
    }

    private String getAttribute(Attributes attributes, String name) throws NamingException {
        Attribute att = attributes.get(name);
        return att!=null ? (String) att.get() : null;
    }

    public HttpResponse doUpdate(
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email,
            @QueryParameter String githubId,
            @QueryParameter String sshKeys
    ) throws Exception {

        final Attributes attrs = new BasicAttributes();

        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);
        attrs.put("employeeNumber",githubId);
        attrs.put("preferredLanguage",sshKeys); // hack since I find it too hard to add custom attributes to LDAP

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

        return new HttpRedirect("done");
    }

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

    private static final Logger LOGGER = Logger.getLogger(Myself.class.getName());
}
