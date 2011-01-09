package test;

import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class Myself {
    private final Application parent;
    private final String dn;
    public String firstName, lastName, email, userId;

    public Myself(Application parent, String dn, Attributes attributes) throws NamingException {
        this.parent = parent;
        this.dn = dn;

        firstName = getAttribute(attributes,"givenName");
        lastName = getAttribute(attributes,"sn");
        email = getAttribute(attributes,"mail");
        userId = getAttribute(attributes,"cn");
    }

    private String getAttribute(Attributes attributes, String name) throws NamingException {
        Attribute att = attributes.get(name);
        return att!=null ? (String) att.get() : null;
    }

    public HttpResponse doUpdate(
            @QueryParameter String firstName,
            @QueryParameter String lastName,
            @QueryParameter String email
    ) throws Exception {

        final Attributes attrs = new BasicAttributes();

        attrs.put("givenName", firstName);
        attrs.put("sn", lastName);
        attrs.put("mail", email);

        LdapContext context = parent.connect();
        try {
            context.modifyAttributes(dn,DirContext.REPLACE_ATTRIBUTE,attrs);
        } finally {
            context.close();
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;

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
        
        return new HttpRedirect("done");
    }
}
