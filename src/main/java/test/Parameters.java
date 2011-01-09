package test;

/**
 * @author Kohsuke Kawaguchi
 */
public interface Parameters {
    /**
     * string like "ou=people,dc=cloudbees,dc=com" that decides where new users are created.
     */
    String newUserBaseDN();

    String managerDN();
    String managerPassword();
    String server();
}
