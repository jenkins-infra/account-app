import org.jenkinsci.account.Application;
import org.jenkinsci.account.WebAppMain;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class BulkImport {
    public static void main(String[] args) throws Exception {
        Application app = new WebAppMain().createApplication();

        Set<String> names = new HashSet<String>();
        SearchControls cons = new SearchControls();
        cons.setReturningAttributes(new String[]{"cn"});
        NamingEnumeration<SearchResult> e = app.connect().search("ou=people,dc=jenkins-ci,dc=org", "(objectClass=inetOrgPerson)", cons);
        while (e.hasMore()) {
            SearchResult r = e.nextElement();
            String cn = (String)r.getAttributes().get("cn").get();
            names.add(cn);
        }

        {// clean up bogus entries
            LdapContext ldap = app.connect();
            for (String name : names) {
                if (!name.toLowerCase().equals(name) || name.contains("@")) {
                    // delete this
                    System.out.println("Deleting "+name);
                    ldap.destroySubcontext("cn=" + name + ",ou=people,dc=jenkins-ci,dc=org");
                }
            }
        }

        File dir = new File(args[0]);
        System.out.println("Listing up "+dir);
        for (File f : dir.listFiles()) {
            if (f.exists() && !f.isDirectory()) {
                String name =f.getName().toLowerCase();
                System.out.println(name);
                if (name.contains("@"))  continue;   // invalid

                if (names.contains(name))   continue;   // already imported

                try {
                    app.createRecord(name,f.getName(),"-",f.getName()+"@java.net");
                } catch (NameAlreadyBoundException x) {
                    // already registered. move on
                }
            }
        }
    }
}
