package org.jenkinsci.account.batch;

import org.jenkinsci.account.Application;
import org.jenkinsci.account.WebAppMain;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import static javax.naming.directory.DirContext.*;
import static org.jenkinsci.account.LdapAbuse.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class UpdateSeniorGroup {
    public static void main(String[] args) throws Exception {
        Application app = new WebAppMain().createApplication();

        SearchControls cons = new SearchControls();
        cons.setReturningAttributes(new String[]{"cn", REGISTRATION_DATE});

        LdapContext con = app.connect();
        try {
            NamingEnumeration<SearchResult> e = con.search("ou=people,dc=jenkins-ci,dc=org", "(&(objectClass=inetOrgPerson)(!("+SENIOR_STATUS+"=Y)))", cons);
            while (e.hasMore()) {
                SearchResult r = e.nextElement();
                String cn = (String) r.getAttributes().get("cn").get();

                System.out.println(cn);

                String dn = r.getNameInNamespace();
                Attribute date = r.getAttributes().get(REGISTRATION_DATE);
                if (isQualifiedAsSenior(date)) {
                    try {
                        con.modifyAttributes("cn=seniors,ou=groups,dc=jenkins-ci,dc=org", ADD_ATTRIBUTE, new BasicAttributes("member", dn));
                    } catch (AttributeInUseException _) {
                        // already a member
                    }
                    con.modifyAttributes(dn, REPLACE_ATTRIBUTE, new BasicAttributes(SENIOR_STATUS, "Y"));
                } else {
                    System.out.println("\tSkipped");
                }
            }
        } finally {
            con.close();
        }
    }

    private static boolean isQualifiedAsSenior(Attribute date) throws Exception {
        if (date==null)     return true;    // older entries do not record this timestamp

        long t = FORMAT.parse((String)date.get()).getTime();
        final long now = System.currentTimeMillis();

        return now - t > TimeUnit.DAYS.toMillis(1);
    }

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
}
