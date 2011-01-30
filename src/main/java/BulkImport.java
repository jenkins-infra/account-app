import org.jenkinsci.account.Application;
import org.jenkinsci.account.WebAppMain;

import javax.naming.NameAlreadyBoundException;
import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class BulkImport {
    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);

        Application app = new WebAppMain().createApplication();
        System.out.println("Listing up "+dir);
        for (File f : dir.listFiles()) {
            if (f.exists() && !f.isDirectory()) {
                String name =f.getName().toLowerCase();
                System.out.println(name);
                if (name.contains("@"))  continue;   // invalid

                try {
                    app.createRecord(name,f.getName(),"-",f.getName()+"@java.net");
                } catch (NameAlreadyBoundException e) {
                    // already registered. move on
                }
            }
        }
    }
}
